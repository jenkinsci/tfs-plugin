package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Item;
import hudson.model.Job;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.extensions.impl.IgnoreNotifyCommit;
import hudson.plugins.tfs.JenkinsEventNotifier;
import hudson.plugins.tfs.TeamEventsEndpoint;
import hudson.plugins.tfs.TeamGlobalStatusAction;
import hudson.plugins.tfs.TeamHookCause;
import hudson.plugins.tfs.TeamPluginGlobalConfig;
import hudson.plugins.tfs.TeamPushTrigger;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.util.ActionHelper;
import hudson.plugins.tfs.util.UriHelper;
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
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;

/**
 * This class abstracts the hook event.
 */
public abstract class AbstractHookEvent {

    private static final Logger LOGGER = Logger.getLogger(AbstractHookEvent.class.getName());

    /**
    * Interface of hook event factory.
    */
    public interface Factory {
        /**
        * Create the factory.
        */
        AbstractHookEvent create();

        /**
        * Get sample request payload.
        */
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
     * @param message a simple description of the event
     * @param detailedMessage a longer description of the event, with some details
     *
     * @return a {@link JSONObject} representing the hook event's output
     */
    public abstract JSONObject perform(final ObjectMapper mapper, final Event serviceHookEvent, final String message, final String detailedMessage);

    static JSONObject fromResponseContributors(final List<GitStatus.ResponseContributor> contributors) {
        final JSONObject result = new JSONObject();
        final JSONArray messages = new JSONArray();
        for (final GitStatus.ResponseContributor contributor : contributors) {
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(stringWriter);
            try {
                contributor.writeBody(printWriter);
                printWriter.flush();
            } finally {
                IOUtils.closeQuietly(printWriter);
            }
            final String contributorMessage = stringWriter.toString();
            messages.add(contributorMessage);
        }
        result.put("messages", messages);
        return result;
    }

    GitStatus.ResponseContributor triggerJob(final GitCodePushedEventArgs gitCodePushedEventArgs, final List<Action> actions, final boolean bypassPolling, final Item project, final SCMTriggerItem scmTriggerItem) {
        if (!(project instanceof AbstractProject && ((AbstractProject) project).isDisabled())) {
            if (project instanceof Job) {
                final Job job = (Job) project;
                final int quietPeriod = scmTriggerItem.getQuietPeriod();
                final String targetUrl = job.getAbsoluteUrl() + job.getNextBuildNumber();

                final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
                final SCMTrigger scmTrigger = TeamEventsEndpoint.findTrigger(job, SCMTrigger.class);
                if (config.isEnableTeamPushTriggerForAllJobs()) {
                    if (scmTrigger == null || !scmTrigger.isIgnorePostCommitHooks()) {
                        // trigger is null OR job does NOT have explicitly opted out of hooks
                        final TeamPushTrigger trigger = new TeamPushTrigger(job);
                        trigger.execute(gitCodePushedEventArgs, actions, bypassPolling);
                        if (bypassPolling) {
                            return new TeamEventsEndpoint.ScheduledResponseContributor(project);
                        } else {
                            return new TeamEventsEndpoint.PollingScheduledResponseContributor(project);
                        }
                    }
                }

                if (scmTrigger != null && !scmTrigger.isIgnorePostCommitHooks()) {
                    // queue build without first polling
                    final Cause cause = new TeamHookCause(gitCodePushedEventArgs.commit);
                    final CauseAction causeAction = new CauseAction(cause);
                    final Action[] actionArray = ActionHelper.create(actions, causeAction);
                    scmTriggerItem.scheduleBuild2(quietPeriod, actionArray);
                    if (gitCodePushedEventArgs instanceof PullRequestMergeCommitCreatedEventArgs) {
                        JenkinsEventNotifier.sendPullRequestBuildStatusEvent((PullRequestMergeCommitCreatedEventArgs) gitCodePushedEventArgs, GitStatusState.Pending, "Jenkins CI build queued", targetUrl, job.getAbsoluteUrl());
                    }

                    return new TeamEventsEndpoint.ScheduledResponseContributor(project);
                }

                boolean shouldRun = false;

                if (job instanceof WorkflowJob) {
                    final FlowDefinition jobDef = ((WorkflowJob) job).getDefinition();
                    if (jobDef instanceof CpsScmFlowDefinition) {
                        final SCM jobSCM = ((CpsScmFlowDefinition) jobDef).getScm();
                        if (jobSCM instanceof GitSCM) {
                            final GitSCM gitJobSCM = (GitSCM) jobSCM;
                            final URIish uri = gitCodePushedEventArgs.getRepoURIish();

                            for (final UserRemoteConfig remoteConfig : gitJobSCM.getUserRemoteConfigs()) {
                                final String jobRepoUrl = remoteConfig.getUrl();

                                if (StringUtils.equalsIgnoreCase(jobRepoUrl, uri.toString())) {
                                    shouldRun = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                TeamPushTrigger pushTrigger = TeamEventsEndpoint.findTrigger(job, TeamPushTrigger.class);
                if (shouldRun && pushTrigger == null) {
                    pushTrigger = new TeamPushTrigger(job);
                }

                if (pushTrigger != null) {
                    pushTrigger.execute(gitCodePushedEventArgs, actions, bypassPolling);
                    if (bypassPolling) {
                        return new TeamEventsEndpoint.ScheduledResponseContributor(project);
                    } else {
                        return  new TeamEventsEndpoint.PollingScheduledResponseContributor(project);
                    }
                }
            }
        }

        return null;
    }

    // TODO: it would be easiest if pollOrQueueFromEvent built a JSONObject directly
    List<GitStatus.ResponseContributor> pollOrQueueFromEvent(final GitCodePushedEventArgs gitCodePushedEventArgs, final List<Action> actions, final boolean bypassPolling) {
        List<GitStatus.ResponseContributor> result = new ArrayList<GitStatus.ResponseContributor>();
        final String commit = gitCodePushedEventArgs.commit;
        if (commit == null) {
            result.add(new GitStatus.MessageResponseContributor("No commits were pushed, skipping further event processing."));
            return result;
        }
        final URIish uri = gitCodePushedEventArgs.getRepoURIish();

        TeamGlobalStatusAction.addIfApplicable(actions);

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
            for (final Item project : Jenkins.getActiveInstance().getAllItems()) {
                final SCMTriggerItem scmTriggerItem = SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(project);

                if (scmTriggerItem == null || scmTriggerItem.getSCMs() == null) {
                    continue;
                }

                if (scmTriggerItem.getSCMs().isEmpty()) {
                    GitStatus.ResponseContributor triggerResult = triggerJob(gitCodePushedEventArgs, actions, bypassPolling, project, scmTriggerItem);
                    if (triggerResult != null) {
                        result.add(triggerResult);
                    }
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
                        for (final URIish remoteURL : repository.getURIs()) {
                            if (UriHelper.areSameGitRepo(uri, remoteURL)) {
                                repositoryMatches = true;
                                totalRepositoryMatches++;
                                break;
                            }
                        }

                        if (!repositoryMatches || git.getExtensions().get(IgnoreNotifyCommit.class) != null) {
                            continue;
                        }

                        GitStatus.ResponseContributor triggerResult = triggerJob(gitCodePushedEventArgs, actions, bypassPolling, project, scmTriggerItem);
                        if (triggerResult != null) {
                            result.add(triggerResult);
                            break;
                        }
                    }
                }
            }
            if (!scmFound) {
                result.add(new GitStatus.MessageResponseContributor("No Git jobs found"));
            } else if (totalRepositoryMatches == 0) {
                final String template = "No Git jobs matched the remote URL '%s' requested by an event.";
                final String message = String.format(template, uri);
                LOGGER.warning(message);
            }

            return result;
        } finally {
            SecurityContextHolder.setContext(old);
        }
    }

}
