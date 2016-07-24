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
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * The endpoint that VSTS will POST to on push, pull request, etc.
 */
@Extension
public class VstsWebHook implements UnprotectedRootAction {

    private static final Logger LOGGER = Logger.getLogger(VstsWebHook.class.getName());

    public static final String URL_NAME = "vsts-hook";

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
            @QueryParameter(required = true) @Nonnull final VstsHookEventName event,
            @StringBodyParameter @Nonnull final String body) {
        LOGGER.log(Level.FINER, "{}/?event={}\n{}", new String[]{URL_NAME, event.name(), body});
        final Object parsedBody = event.parse(body);
        List<? extends GitStatus.ResponseContributor> contributors = null;
        switch (event) {
            case PING:
                final String message = "Pong from the Jenkins TFS plugin! Here's your body:\n" + parsedBody;
                final GitStatus.MessageResponseContributor contributor;
                contributor = new GitStatus.MessageResponseContributor(message);
                contributors = Collections.singletonList(contributor);
            case BUILD_COMPLETED:
                break;
            case GIT_CODE_PUSHED:
                final GitCodePushedEventArgs gitCodePushedEventArgs = (GitCodePushedEventArgs) parsedBody;
                contributors = gitCodePushed(gitCodePushedEventArgs);
                break;
            case TFVC_CODE_CHECKED_IN:
                break;
            case PULL_REQUEST_MERGE_COMMIT_CREATED:
                final PullRequestMergeCommitCreatedEventArgs pullRequestMergeCommitCreatedEventArgs = (PullRequestMergeCommitCreatedEventArgs) parsedBody;
                contributors = pullRequestMergeCommitCreated(pullRequestMergeCommitCreatedEventArgs);
                break;
            case DEPLOYMENT_COMPLETED:
                break;
        }
        if (contributors == null) {
            return HttpResponses.error(SC_BAD_REQUEST, "Not implemented");
        }
        else {
            final List<? extends GitStatus.ResponseContributor> finalContributors = contributors;
            return new HttpResponse() {
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
                        throws IOException, ServletException {
                    rsp.setStatus(SC_OK);
                    rsp.setContentType(MediaType.TEXT_PLAIN);
                    for (GitStatus.ResponseContributor c : finalContributors) {
                        c.addHeaders(req, rsp);
                    }
                    rsp.setCharacterEncoding(MediaType.UTF_8.name());
                    PrintWriter w = rsp.getWriter();
                    for (GitStatus.ResponseContributor c : finalContributors) {
                        c.writeBody(req, rsp, w);
                    }
                }
            };

        }
    }

    public List<GitStatus.ResponseContributor> pullRequestMergeCommitCreated(final PullRequestMergeCommitCreatedEventArgs args) {
        // TODO: implement
        return null;
    }

    public List<GitStatus.ResponseContributor> gitCodePushed(final GitCodePushedEventArgs gitCodePushedEventArgs) {
        // TODO: add extension point for this event, then extract current implementation as extension(s)

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
                LOGGER.severe("Jenkins.getInstance() is null in VstsWebHook.gitCodePushed");
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
                                        final Cause cause = new VstsHookCause(commit);
                                        final CauseAction causeAction = new CauseAction(cause);
                                        final CommitParameterAction commitParameterAction = new CommitParameterAction(gitCodePushedEventArgs);
                                        scmTriggerItem.scheduleBuild2(quietPeriod, causeAction, commitParameterAction);
                                        result.add(new ScheduledResponseContributor(project));
                                        triggered = true;
                                    }
                                }
                                if (!triggered) {
                                    final VstsPushTrigger pushTrigger = findTrigger(job, VstsPushTrigger.class);
                                    if (pushTrigger != null) {
                                        pushTrigger.execute(gitCodePushedEventArgs);
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
