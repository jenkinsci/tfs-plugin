package hudson.plugins.tfs.model;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Queue;
import hudson.model.SimpleParameterDefinition;
import hudson.model.queue.ScheduleResult;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.TeamBuildEndpoint;
import hudson.plugins.tfs.util.MediaType;
import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildCommand extends AbstractCommand {

    private static final Action[] EMPTY_ACTION_ARRAY = new Action[0];
    private static final String BUILD_REPOSITORY_PROVIDER = "Build.Repository.Provider";
    private static final String BUILD_REPOSITORY_URI = "Build.Repository.Uri";
    private static final String BUILD_REPOSITORY_NAME = "Build.Repository.Name";
    private static final String SYSTEM_TEAM_PROJECT = "System.TeamProject";
    private static final String BUILD_SOURCE_VERSION = "Build.SourceVersion";
    private static final String BUILD_REQUESTED_FOR = "Build.RequestedFor";
    private static final String SYSTEM_TEAM_FOUNDATION_COLLECTION_URI = "System.TeamFoundationCollectionUri";
    private static final String COMMIT_ID = "commitId";
    private static final String PULL_REQUEST_ID = "pullRequestId";

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

    protected JSONObject innerPerform(final AbstractProject project, final TimeDuration delay, final List<Action> extraActions) {
        final JSONObject result = new JSONObject();

        final Jenkins jenkins = Jenkins.getInstance();
        final Queue queue = jenkins.getQueue();
        final Cause cause = new Cause.UserIdCause();
        final CauseAction causeAction = new CauseAction(cause);
        final List<Action> actions = new ArrayList<Action>();
        actions.add(causeAction);

        actions.addAll(extraActions);

        final Action[] actionArray = actions.toArray(EMPTY_ACTION_ARRAY);
        final ScheduleResult scheduleResult = queue.schedule2(project, delay.getTime(), actionArray);
        final Queue.Item item = scheduleResult.getItem();
        if (item != null) {
            result.put("created", jenkins.getRootUrl() + item.getUrl());
        }
        return result;
    }

    @Override
    public JSONObject perform(final AbstractProject project, final StaplerRequest req, final JSONObject requestPayload, final TimeDuration delay) {

        // These values are for optional parameters of the same name, for the git.pullrequest.merged event
        String commitId = null;
        String pullRequestId = null;

        final List<Action> actions = new ArrayList<Action>();

        if (requestPayload.containsKey(TeamBuildEndpoint.TEAM_BUILD)) {
            final HashMap<String, String> teamBuildParameters = new HashMap<String, String>();
            final JSONObject variables = requestPayload.getJSONObject(TeamBuildEndpoint.TEAM_BUILD);
            for (final String key : ((Map<String, Object>)variables).keySet()) {
                final String value = variables.getString(key);
                teamBuildParameters.put(key, value);
            }
            contributeTeamBuildParameterActions(teamBuildParameters, actions);
        }
        else if (requestPayload.containsKey(TeamBuildEndpoint.TEAM_EVENT)) {
            final JSONObject teamEventJson = requestPayload.getJSONObject(TeamBuildEndpoint.TEAM_EVENT);
            final String eventType = teamEventJson.getString("eventType");
            if ("git.push".equals(eventType)) {
                final GitCodePushedEventArgs args = GitPushEvent.decodeGitPush(teamEventJson);
                final Action action = new CommitParameterAction(args);
                actions.add(action);
            }
            else if ("git.pullrequest.merged".equals(eventType)) {
                final PullRequestMergeCommitCreatedEventArgs args = GitPullRequestMergedEvent.decodeGitPullRequestMerged(teamEventJson);
                // record the values for the special optional parameters
                commitId = args.commit;
                pullRequestId = Integer.toString(args.pullRequestId, 10);
                final Action action = new PullRequestParameterAction(args);
                actions.add(action);
            }
        }

        //noinspection UnnecessaryLocalVariable
        final Job<?, ?> job = project;
        final ParametersDefinitionProperty pp = job.getProperty(ParametersDefinitionProperty.class);
        if (pp != null && requestPayload.containsKey(TeamBuildEndpoint.PARAMETER)) {
            final List<ParameterValue> values = new ArrayList<ParameterValue>();
            final JSONArray a = requestPayload.getJSONArray(TeamBuildEndpoint.PARAMETER);

            for (final Object o : a) {
                final JSONObject jo = (JSONObject) o;
                final String name = jo.getString("name");

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
                else if (name.equals(PULL_REQUEST_ID) && pullRequestId != null & d instanceof SimpleParameterDefinition) {
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

        return innerPerform(project, delay, actions);
    }

    static boolean isTeamGit(final Map<String, String> teamBuildParameters) {
        if (teamBuildParameters.containsKey(BUILD_REPOSITORY_PROVIDER)) {
            final String provider = teamBuildParameters.get(BUILD_REPOSITORY_PROVIDER);
            return "TfGit".equalsIgnoreCase(provider)
                    || "TfsGit".equalsIgnoreCase(provider);
        }
        return false;
    }

    static void contributeTeamBuildParameterActions(final Map<String, String> teamBuildParameters, final List<Action> actions) {
        if (isTeamGit(teamBuildParameters)) {
            final String collectionUriString = teamBuildParameters.get(SYSTEM_TEAM_FOUNDATION_COLLECTION_URI);
            final URI collectionUri = URI.create(collectionUriString);
            final String repoUriString = teamBuildParameters.get(BUILD_REPOSITORY_URI);
            final URI repoUri = URI.create(repoUriString);
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
    }
}
