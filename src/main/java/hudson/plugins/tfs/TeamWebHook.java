package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.model.AbstractHookEvent;
import hudson.plugins.tfs.model.PingHookEvent;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;
import hudson.plugins.tfs.util.MediaType;
import hudson.plugins.git.extensions.impl.IgnoreNotifyCommit;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.util.StringBodyParameter;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.triggers.SCMTrigger;
import hudson.triggers.Trigger;
import jenkins.model.Jenkins;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * The endpoint that TFS/Team Services will POST to on Git code push, pull request merge commit creation, etc.
 */
@Extension
public class TeamWebHook implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(TeamWebHook.class.getName());
    private static final Map<String, AbstractHookEvent.Factory> HOOK_EVENT_FACTORIES_BY_NAME;

    static {
        final Map<String, AbstractHookEvent.Factory> eventMap =
                new TreeMap<String, AbstractHookEvent.Factory>(String.CASE_INSENSITIVE_ORDER);
        eventMap.put("ping", new PingHookEvent.Factory());
        HOOK_EVENT_FACTORIES_BY_NAME = Collections.unmodifiableMap(eventMap);
    }

    static final String EVENT_NAME = "eventName";
    static final String REQUEST_PAYLOAD = "requestPayload";

    public static final String URL_NAME = "team-events";

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }

    @RequirePOST
    public HttpResponse doIndex(
            final HttpServletRequest request,
            @StringBodyParameter @Nonnull final String body) {
        final JSONObject eventAndPayload = JSONObject.fromObject(body);
        final String eventName = eventAndPayload.getString(EVENT_NAME);
        try {
            if (StringUtils.isBlank(eventName)) {
                throw new IllegalArgumentException("eventName is missing");
            }
            LOGGER.log(Level.FINER, "{}\n{}", new String[]{URL_NAME, body});
            if (!HOOK_EVENT_FACTORIES_BY_NAME.containsKey(eventName)) {
                final String template = "Event '%s' is not implemented";
                final String message = String.format(template, eventName);
                throw new IllegalArgumentException(message);
            }
            if (!eventAndPayload.containsKey(REQUEST_PAYLOAD)) {
                throw new IllegalArgumentException("requestPayload is missing");
            }
            final AbstractHookEvent.Factory factory = HOOK_EVENT_FACTORIES_BY_NAME.get(eventName);
            final JSONObject requestPayload = eventAndPayload.getJSONObject(REQUEST_PAYLOAD);
            final AbstractHookEvent hookEvent = factory.create(requestPayload);
            hookEvent.run();
            final JSONObject response = hookEvent.getResponse();
            return new HttpResponse() {
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
                        throws IOException, ServletException {
                    rsp.setStatus(SC_OK);
                    rsp.setContentType(MediaType.APPLICATION_JSON_UTF_8);
                    final PrintWriter w = rsp.getWriter();
                    final String responseJsonString = response.toString();
                    w.print(responseJsonString);
                    w.println();
                }
            };
        }
        catch (final IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "IllegalArgumentException", e);
            // TODO: serialize it to JSON and set as the response
            return HttpResponses.error(SC_BAD_REQUEST, e.getMessage());
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while performing reaction to event.", e);
            // TODO: serialize it to JSON and set as the response
            return HttpResponses.error(SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public List<GitStatus.ResponseContributor> pullRequestMergeCommitCreated(final PullRequestMergeCommitCreatedEventArgs args) {
        final PullRequestParameterAction action = new PullRequestParameterAction(args);
        // TODO: add extension point for this event, then extract current implementation as extension(s)

        return pollOrQueueFromEvent(args, action);
    }

    public List<GitStatus.ResponseContributor> gitCodePushed(final GitCodePushedEventArgs gitCodePushedEventArgs) {
        final CommitParameterAction commitParameterAction = new CommitParameterAction(gitCodePushedEventArgs);
        // TODO: add extension point for this event, then extract current implementation as extension(s)

        return pollOrQueueFromEvent(gitCodePushedEventArgs, commitParameterAction);
    }

    List<GitStatus.ResponseContributor> pollOrQueueFromEvent(final GitCodePushedEventArgs gitCodePushedEventArgs, final CommitParameterAction commitParameterAction) {
        List<GitStatus.ResponseContributor> result = new ArrayList<GitStatus.ResponseContributor>();
        final String commit = gitCodePushedEventArgs.commit;
        final URIish uri = gitCodePushedEventArgs.getRepoURIish();

        // run in high privilege to see all the projects anonymous users don't see.
        // this is safe because when we actually schedule a build, it's a build that can
        // happen at some random time anyway.
        SecurityContext old = ACL.impersonate(ACL.SYSTEM);
        try {

            boolean scmFound = false;
            final Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                LOGGER.severe("Jenkins.getInstance() is null");
                return result;
            }
            for (final Item project : Jenkins.getInstance().getAllItems()) {
                final SCMTriggerItem scmTriggerItem = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(project);
                if (scmTriggerItem == null) {
                    continue;
                }
                for (final SCM scm : scmTriggerItem.getSCMs()) {
                    if (!(scm instanceof GitSCM)) {
                        continue;
                    }
                    final GitSCM git = (GitSCM) scm;
                    scmFound = true;

                    for (final RemoteConfig repository : git.getRepositories()) {
                        boolean repositoryMatches = false;
                        for (URIish remoteURL : repository.getURIs()) {
                            if (GitStatus.looselyMatches(uri, remoteURL)) {
                                repositoryMatches = true;
                                break;
                            }
                        }

                        if (!repositoryMatches || git.getExtensions().get(IgnoreNotifyCommit.class)!=null) {
                            continue;
                        }

                        if (!(project instanceof AbstractProject && ((AbstractProject) project).isDisabled())) {
                            if (project instanceof Job) {
                                // TODO: Add default parameters defined in the job
                                final Job job = (Job) project;

                                boolean triggered = false;
                                if (!triggered) {
                                    // TODO: check global override here
                                }

                                if (!triggered) {
                                    final SCMTrigger scmTrigger = findTrigger(job, SCMTrigger.class);
                                    if (scmTrigger != null && !scmTrigger.isIgnorePostCommitHooks()) {
                                        // queue build without first polling
                                        final int quietPeriod = scmTriggerItem.getQuietPeriod();
                                        final Cause cause = new TeamHookCause(commit);
                                        final CauseAction causeAction = new CauseAction(cause);
                                        scmTriggerItem.scheduleBuild2(quietPeriod, causeAction, commitParameterAction);
                                        result.add(new ScheduledResponseContributor(project));
                                        triggered = true;
                                    }
                                }
                                if (!triggered) {
                                    final TeamPushTrigger pushTrigger = findTrigger(job, TeamPushTrigger.class);
                                    if (pushTrigger != null) {
                                        pushTrigger.execute(gitCodePushedEventArgs, commitParameterAction);
                                        result.add(new PollingScheduledResponseContributor(project));
                                        triggered = true;
                                    }
                                }
                                if (triggered) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!scmFound) {
                result.add(new GitStatus.MessageResponseContributor("No Git jobs found"));
            }

            return result;
        }
        finally {
            SecurityContextHolder.setContext(old);
        }
    }

    private static <T extends Trigger> T findTrigger(final Job<?, ?> job, final Class<T> tClass) {
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            final ParameterizedJobMixIn.ParameterizedJob pJob = (ParameterizedJobMixIn.ParameterizedJob) job;
            for (final Trigger trigger : pJob.getTriggers().values()) {
                if (tClass.isInstance(trigger)) {
                    return tClass.cast(trigger);
                }
            }
        }
        return null;
    }

    /**
     * A response contributor for triggering polling of a project.
     *
     * @since 1.4.1
     */
    private static class PollingScheduledResponseContributor extends GitStatus.ResponseContributor {
        /**
         * The project
         */
        private final Item project;

        /**
         * Constructor.
         *
         * @param project the project.
         */
        public PollingScheduledResponseContributor(Item project) {
            this.project = project;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addHeaders(StaplerRequest req, StaplerResponse rsp) {
            rsp.addHeader("Triggered", project.getAbsoluteUrl());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeBody(PrintWriter w) {
            w.println("Scheduled polling of " + project.getFullDisplayName());
        }
    }

    private static class ScheduledResponseContributor extends GitStatus.ResponseContributor {
        /**
         * The project
         */
        private final Item project;

        /**
         * Constructor.
         *
         * @param project the project.
         */
        public ScheduledResponseContributor(Item project) {
            this.project = project;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addHeaders(StaplerRequest req, StaplerResponse rsp) {
            rsp.addHeader("Triggered", project.getAbsoluteUrl());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void writeBody(PrintWriter w) {
            w.println("Scheduled " + project.getFullDisplayName());
        }
    }
}
