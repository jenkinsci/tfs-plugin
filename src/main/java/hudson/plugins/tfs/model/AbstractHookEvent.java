package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.git.extensions.impl.IgnoreNotifyCommit;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.TeamCollectionConfiguration;
import hudson.plugins.tfs.TeamEventsEndpoint;
import hudson.plugins.tfs.TeamHookCause;
import hudson.plugins.tfs.TeamPushTrigger;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.util.StringHelper;
import hudson.scm.SCM;
import hudson.security.ACL;
import hudson.triggers.SCMTrigger;
import jenkins.model.Jenkins;
import jenkins.triggers.SCMTriggerItem;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractHookEvent {

    private static final Logger LOGGER = Logger.getLogger(AbstractHookEvent.class.getName());

    public interface Factory {
        AbstractHookEvent create();
        String getSampleRequestPayload();
    }

    /**
     * Actually do the work of the hook event, using the supplied
     * {@code mapper} to convert the event's data from the supplied {@code serviceHookEvent}
     * and returning the output as a {@link JSONObject}.
     *
     * @param mapper an {@link ObjectMapper} instance to use to convert the {@link Event#resource}
     * @param serviceHookEvent an {@link Event} that represents the request payload
     *                         and from which the {@link Event#resource} can be obtained
     *
     * @return a {@link JSONObject} representing the hook event's output
     */
    public abstract JSONObject perform(final ObjectMapper mapper, final Event serviceHookEvent);

    static JSONObject fromResponseContributors(final List<GitStatus.ResponseContributor> contributors) {
        final JSONObject result = new JSONObject();
        final JSONArray messages = new JSONArray();
        for (final GitStatus.ResponseContributor contributor : contributors) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            try {
                contributor.writeBody(printWriter);
                printWriter.flush();
            }
            finally {
                IOUtils.closeQuietly(printWriter);
            }
            final String contributorMessage = stringWriter.toString();
            messages.add(contributorMessage);
        }
        result.put("messages", messages);
        return result;
    }

    // TODO: it would be easiest if pollOrQueueFromEvent built a JSONObject directly
    List<GitStatus.ResponseContributor> pollOrQueueFromEvent(final GitCodePushedEventArgs gitCodePushedEventArgs, final CommitParameterAction commitParameterAction, final boolean bypassPolling) {
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
            int totalRepositoryMatches = 0;
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
                                totalRepositoryMatches++;
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
                                final int quietPeriod = scmTriggerItem.getQuietPeriod();

                                boolean triggered = false;
                                if (!triggered) {
                                    // TODO: check global override here
                                }
                                if (!triggered) {
                                    final SCMTrigger scmTrigger = TeamEventsEndpoint.findTrigger(job, SCMTrigger.class);
                                    if (scmTrigger != null && !scmTrigger.isIgnorePostCommitHooks()) {
                                        // queue build without first polling
                                        final Cause cause = new TeamHookCause(commit);
                                        final CauseAction causeAction = new CauseAction(cause);
                                        scmTriggerItem.scheduleBuild2(quietPeriod, causeAction, commitParameterAction);
                                        result.add(new TeamEventsEndpoint.ScheduledResponseContributor(project));
                                        triggered = true;
                                    }
                                }
                                if (!triggered) {
                                    final TeamPushTrigger pushTrigger = TeamEventsEndpoint.findTrigger(job, TeamPushTrigger.class);
                                    if (pushTrigger != null) {
                                        pushTrigger.execute(gitCodePushedEventArgs, commitParameterAction, bypassPolling);
                                        final GitStatus.ResponseContributor response;
                                        if (bypassPolling) {
                                            response = new TeamEventsEndpoint.ScheduledResponseContributor(project);
                                        }
                                        else {
                                            response = new TeamEventsEndpoint.PollingScheduledResponseContributor(project);
                                        }
                                        result.add(response);
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
            else if (totalRepositoryMatches == 0) {
                final String template = "No Git jobs matched the remote URL '%s' requested by an event.";
                final String message = String.format(template, uri);
                LOGGER.warning(message);
            }

            return result;
        }
        finally {
            SecurityContextHolder.setContext(old);
        }
    }

    // TODO: find a better home for this method
    static boolean isTeamServicesNearMatch(final URIish a, final URIish b) {
        final String aHost = a.getHost();
        final String bHost = b.getHost();
        if (TeamCollectionConfiguration.isTeamServices(aHost)
                && TeamCollectionConfiguration.isTeamServices(bHost)) {
            final String aPath = normalizePath(a.getPath());
            final String bPath = normalizePath(b.getPath());
            if (StringHelper.endsWithIgnoreCase(aPath, bPath)
                    || StringHelper.endsWithIgnoreCase(bPath, aPath)) {
                return true;
            }
        }
        return false;
    }

    static String normalizePath(String path) {
        if(path.startsWith("/")) {
            path = path.substring(1);
        }

        if(path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

}
