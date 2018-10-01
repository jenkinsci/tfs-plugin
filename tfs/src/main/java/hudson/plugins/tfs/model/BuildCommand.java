//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPush;
import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Executor;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Queue;
import hudson.model.Run;
import hudson.model.SimpleParameterDefinition;
import hudson.model.queue.ScheduleResult;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.TeamBuildDetailsAction;
import hudson.plugins.tfs.TeamBuildEndpoint;
import hudson.plugins.tfs.TeamGlobalStatusAction;
import hudson.plugins.tfs.TeamPullRequestMergedDetailsAction;
import hudson.plugins.tfs.UnsupportedIntegrationAction;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.util.ActionHelper;
import hudson.plugins.tfs.util.MediaType;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BuildCommand extends AbstractCommand {

    private static final Logger LOGGER = Logger.getLogger(BuildCommand.class.getName());

    private static final String BUILD_REPOSITORY_PROVIDER = "Build.Repository.Provider";
    private static final String BUILD_REPOSITORY_URI = "Build.Repository.Uri";
    private static final String BUILD_REPOSITORY_NAME = "Build.Repository.Name";
    private static final String SYSTEM_TEAM_PROJECT = "System.TeamProject";
    private static final String BUILD_SOURCE_VERSION = "Build.SourceVersion";
    private static final String BUILD_REQUESTED_FOR = "Build.RequestedFor";
    private static final String SYSTEM_TEAM_FOUNDATION_COLLECTION_URI = "System.TeamFoundationCollectionUri";
    private static final String COMMIT_ID = "commitId";
    private static final String PULL_REQUEST_ID = "pullRequestId";
    private static final String UNSUPPORTED_TEMPLATE =
            "The rich integration with TFS/Team Services is not supported. Reason: %s";

    public static String formatUnsupportedReason(final String reason) {
        return String.format(UNSUPPORTED_TEMPLATE, reason);
    }

    public static class Factory implements AbstractCommand.Factory {
        @Override
        public AbstractCommand create() {
            return new BuildCommand();
        }

        @Override
        public String getSampleRequestPayload() {
            final Class<? extends Factory> me = this.getClass();
            final InputStream stream = me.getResourceAsStream("BuildCommand.json");
            try {
                return IOUtils.toString(stream, MediaType.UTF_8);
            }
            catch (final IOException e) {
                throw new Error(e);
            }
            finally {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    protected JSONObject innerPerform(final Job job, final BuildableItem buildableItem, final TimeDuration delay, final List<Action> extraActions) {
        final JSONObject result = new JSONObject();

        final Jenkins jenkins = Jenkins.getActiveInstance();
        final Queue queue = jenkins.getQueue();
        final Cause cause = new Cause.UserIdCause();
        final CauseAction causeAction = new CauseAction(cause);
        final Action[] actionArray = ActionHelper.create(extraActions, causeAction);
        for (Action a : extraActions) {
            if (a instanceof TeamPullRequestMergedDetailsAction) {
                cancelPreviousPullRequestBuilds(job, (TeamPullRequestMergedDetailsAction) a, queue);
            }
        }
        final ScheduleResult scheduleResult = queue.schedule2(buildableItem, delay.getTime(), actionArray);
        final Queue.Item item = scheduleResult.getItem();
        if (item != null) {
            result.put("created", jenkins.getRootUrl() + item.getUrl());
        }
        return result;
    }

    @Override
    public JSONObject perform(final Job<?, ?> job, final BuildableItem buildableItem, final StaplerRequest req,
                              final JSONObject requestPayload, final ObjectMapper mapper,
                              final TeamBuildPayload teamBuildPayload, final TimeDuration delay) {

        // These values are for optional parameters of the same name, for the git.pullrequest.merged event
        String commitId = null;
        String pullRequestId = null;

        final List<Action> actions = new ArrayList<Action>();

        if (teamBuildPayload.BuildVariables != null) {
            contributeTeamBuildParameterActions(teamBuildPayload.BuildVariables, actions);
        }
        else if (teamBuildPayload.ServiceHookEvent != null) {
            final Event event = teamBuildPayload.ServiceHookEvent;
            final String eventType = event.getEventType();
            final Object resource = event.getResource();
            if ("git.push".equals(eventType)) {
                final GitPush gitPush = mapper.convertValue(resource, GitPush.class);
                final GitCodePushedEventArgs args = GitPushEvent.decodeGitPush(gitPush, event);
                final Action action = new CommitParameterAction(args);
                actions.add(action);
                TeamGlobalStatusAction.addIfApplicable(actions);
            }
            else if ("git.pullrequest.merged".equals(eventType)) {
                final GitPullRequestEx gitPullRequest = mapper.convertValue(resource, GitPullRequestEx.class);
                final PullRequestMergeCommitCreatedEventArgs args = GitPullRequestMergedEvent.decodeGitPullRequest(gitPullRequest, event);
                // record the values for the special optional parameters
                commitId = args.commit;
                pullRequestId = Integer.toString(args.pullRequestId, 10);
                final Action action = new PullRequestParameterAction(args);
                actions.add(action);
                final String message = event.getMessage().getText();
                final String detailedMessage = event.getDetailedMessage().getText();
                final Action teamPullRequestMergedDetailsAction = new TeamPullRequestMergedDetailsAction(gitPullRequest, message, detailedMessage, args.collectionUri.toString());
                actions.add(teamPullRequestMergedDetailsAction);
                TeamGlobalStatusAction.addIfApplicable(actions);
            }
        }

        final ParametersDefinitionProperty pp = job.getProperty(ParametersDefinitionProperty.class);
        if (pp != null && requestPayload.containsKey(TeamBuildEndpoint.PARAMETER)) {
            final List<ParameterValue> values = new ArrayList<ParameterValue>();
            final JSONArray a = requestPayload.getJSONArray(TeamBuildEndpoint.PARAMETER);
            final List<String> parameterNames = new ArrayList<>();

            for (final Object o : a) {
                final JSONObject jo = (JSONObject) o;
                final String name = jo.getString("name");
                parameterNames.add(name);

                final ParameterDefinition d = pp.getParameterDefinition(name);
                if (d == null)
                    throw new IllegalArgumentException("No such parameter definition: " + name);
                final ParameterValue parameterValue;
                // commitId and pullRequestId are special and override any user-provided value
                // when the team-event's eventType was "git.pullrequest.merged"
                if (name.equals(COMMIT_ID) && commitId != null && d instanceof SimpleParameterDefinition) {
                    final SimpleParameterDefinition spd = (SimpleParameterDefinition) d;
                    parameterValue = spd.createValue(commitId);
                    // erase value to avoid adding it a second time
                    commitId = null;
                }
                else if (name.equals(PULL_REQUEST_ID) && pullRequestId != null && d instanceof SimpleParameterDefinition) {
                    final SimpleParameterDefinition spd = (SimpleParameterDefinition) d;
                    parameterValue = spd.createValue(pullRequestId);
                    // erase value to avoid adding it a second time
                    pullRequestId = null;
                }
                else {
                    parameterValue = d.createValue(req, jo);
                }
                if (parameterValue != null) {
                    values.add(parameterValue);
                }
                else {
                    throw new IllegalArgumentException("Cannot retrieve the parameter value: " + name);
                }
            }

            //Pick up default build parameters that have not been overridden
            for(final ParameterDefinition paramDef : pp.getParameterDefinitions()) {
                if(!parameterNames.contains(paramDef.getName()) && paramDef instanceof SimpleParameterDefinition){
                    values.add(paramDef.getDefaultParameterValue());
                }
            }

            // typical case: set optional "git.pullrequest.merged" parameters
            if (commitId != null) {
                final ParameterDefinition d = pp.getParameterDefinition(COMMIT_ID);
                if (d != null && d instanceof SimpleParameterDefinition) {
                    final SimpleParameterDefinition spd = (SimpleParameterDefinition) d;
                    final ParameterValue parameterValue = spd.createValue(commitId);
                    values.add(parameterValue);
                }
            }
            if (pullRequestId != null) {
                final ParameterDefinition d = pp.getParameterDefinition(PULL_REQUEST_ID);
                if (d != null && d instanceof SimpleParameterDefinition) {
                    final SimpleParameterDefinition spd = (SimpleParameterDefinition) d;
                    final ParameterValue parameterValue = spd.createValue(pullRequestId);
                    values.add(parameterValue);
                }
            }

            final ParametersAction action = new ParametersAction(values);
            actions.add(action);
        }

        return innerPerform(job, buildableItem, delay, actions);
    }

    private void cancelPreviousPullRequestBuilds(Job job, TeamPullRequestMergedDetailsAction pullReqeuestMergedDetails, Queue queue) {
        RunList<?> allBuilds = job.getBuilds();

        for (Run run : allBuilds) {
            TeamPullRequestMergedDetailsAction cause = run.getAction(TeamPullRequestMergedDetailsAction.class);
            if (cause != null && run.isBuilding()) {
                if (cause instanceof TeamPullRequestMergedDetailsAction &&
                        cause.gitPullRequest.getPullRequestId() == pullReqeuestMergedDetails.gitPullRequest.getPullRequestId()) {
                    LOGGER.info("Canceling previously triggered Job: " + run.getFullDisplayName());

                    Executor executor = run.getExecutor();
                    if (executor != null)
                        executor.doStop();

                    Queue.Item item = queue.getItem(run.getQueueId());
                    if (item != null)
                        queue.cancel(item);
                }
            }
        }
    }

    static void contributeTeamBuildParameterActions(final Map<String, String> teamBuildParameters, final List<Action> actions) {
        final Action teamBuildDetails = new TeamBuildDetailsAction(teamBuildParameters);
        actions.add(teamBuildDetails);
        if (teamBuildParameters.containsKey(BUILD_REPOSITORY_PROVIDER)) {
            final String provider = teamBuildParameters.get(BUILD_REPOSITORY_PROVIDER);
            final boolean isTeamGit = "TfGit".equalsIgnoreCase(provider)
                    || "TfsGit".equalsIgnoreCase(provider);
            if (isTeamGit) {
            	// "Build.Repository.Uri" is null/whitespace/empty when the 'Jenkins Queue Job' task runs in TFS Release Management.
            	// In this case, do not reference a CommitParameterAction in the actions reported as unsupported.
                final String repoUriString = teamBuildParameters.get(BUILD_REPOSITORY_URI);
                if (StringUtils.isNotBlank(repoUriString)) {
                    final URI repoUri = URI.create(repoUriString);
                    final String collectionUriString = teamBuildParameters.get(SYSTEM_TEAM_FOUNDATION_COLLECTION_URI);
                    final URI collectionUri = URI.create(collectionUriString);
                    final String projectId = teamBuildParameters.get(SYSTEM_TEAM_PROJECT);
                    final String repoId = teamBuildParameters.get(BUILD_REPOSITORY_NAME);
                    final String commit = teamBuildParameters.get(BUILD_SOURCE_VERSION);
                    final String pushedBy = teamBuildParameters.get(BUILD_REQUESTED_FOR);
                    final GitCodePushedEventArgs args = new GitCodePushedEventArgs();
                    args.collectionUri = collectionUri;
                    args.repoUri = repoUri;
                    args.projectId = projectId;
                    args.repoId = repoId;
                    args.commit = commit;
                    args.pushedBy = pushedBy;
                    final CommitParameterAction action = new CommitParameterAction(args);
                    actions.add(action);
                }

                UnsupportedIntegrationAction.addToBuild(actions, "Posting build status is not supported for builds triggered by the 'Jenkins Queue Job' task.");
            }
            else {
                final String reason = String.format(
                        "The '%s' build variable has a value of '%s', which is not supported.", BUILD_REPOSITORY_PROVIDER, provider);
                UnsupportedIntegrationAction.addToBuild(actions, reason);
                LOGGER.warning(formatUnsupportedReason(reason));
            }
        }
        else {
            final String reason = String.format(
                    "There was no value provided for the '%s' build variable.",
                    BUILD_REPOSITORY_PROVIDER);
            UnsupportedIntegrationAction.addToBuild(actions, reason);
            LOGGER.warning(formatUnsupportedReason(reason));
        }
    }
}
