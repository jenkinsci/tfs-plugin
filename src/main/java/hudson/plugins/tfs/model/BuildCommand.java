package hudson.plugins.tfs.model;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Queue;
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
import org.jfree.data.Values;
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
    protected static final String TEAM_BUILD_PREFIX = "_team-build_";
    protected static final int TEAM_BUILD_PREFIX_LENGTH = TEAM_BUILD_PREFIX.length();
    private static final String BUILD_SOURCE_BRANCH = "Build.SourceBranch";
    private static final String BUILD_REPOSITORY_PROVIDER = "Build.Repository.Provider";
    private static final String REFS_PULL_SLASH = "refs/pull/";
    private static final int REFS_PULL_SLASH_LENGTH = REFS_PULL_SLASH.length();
    private static final String BUILD_REPOSITORY_URI = "Build.Repository.Uri";
    private static final String BUILD_REPOSITORY_NAME = "Build.Repository.Name";
    private static final String SYSTEM_TEAM_PROJECT = "System.TeamProject";
    private static final String BUILD_SOURCE_VERSION = "Build.SourceVersion";
    private static final String BUILD_REQUESTED_FOR = "Build.RequestedFor";
    private static final String SYSTEM_TEAM_FOUNDATION_COLLECTION_URI = "System.TeamFoundationCollectionUri";

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

        final List<Action> actions = new ArrayList<Action>();
        if (requestPayload.containsKey(TeamBuildEndpoint.TEAM_PARAMETERS)) {
            final JSONObject eventArgsJson = requestPayload.getJSONObject(TeamBuildEndpoint.TEAM_PARAMETERS);
            final CommitParameterAction action;
            // TODO: improve the payload detection!
            if (eventArgsJson.containsKey("pullRequestId")) {
                final PullRequestMergeCommitCreatedEventArgs args = PullRequestMergeCommitCreatedEventArgs.fromJsonObject(eventArgsJson);
                action = new PullRequestParameterAction(args);
            }
            else {
                final GitCodePushedEventArgs args = GitCodePushedEventArgs.fromJsonObject(eventArgsJson);
                action = new CommitParameterAction(args);
            }
            actions.add(action);
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
                final ParameterValue parameterValue = d.createValue(req, jo);
                if (parameterValue != null) {
                    values.add(parameterValue);
                }
                else {
                    throw new IllegalArgumentException("Cannot retrieve the parameter value: " + name);
                }
            }
            final ParametersAction action = new ParametersAction(values);
            actions.add(action);
        }

        return innerPerform(project, delay, actions);
    }

    @Override
    public JSONObject perform(final AbstractProject project, final StaplerRequest request, final TimeDuration delay) {

        final List<Action> actions = new ArrayList<Action>();

        final HashMap<String, String> teamParameters = new HashMap<String, String>();

        final Map<String, String[]> parameters = request.getParameterMap();
        for (final Map.Entry<String, String[]> entry : parameters.entrySet()) {
            final String paramName = entry.getKey();
            if (paramName.startsWith(TEAM_BUILD_PREFIX)) {
                final String teamParamName = paramName.substring(TEAM_BUILD_PREFIX_LENGTH);
                final String[] valueArray = entry.getValue();
                if (valueArray == null || valueArray.length != 1) {
                    throw new IllegalArgumentException(String.format("Expected exactly 1 value for parameter '%s'.", teamParamName));
                }
                teamParameters.put(teamParamName, valueArray[0]);
            }
        }

        if (teamParameters.containsKey(BUILD_REPOSITORY_PROVIDER) && "TfGit".equalsIgnoreCase(teamParameters.get(BUILD_REPOSITORY_PROVIDER))) {
            final String collectionUriString = teamParameters.get(SYSTEM_TEAM_FOUNDATION_COLLECTION_URI);
            final URI collectionUri = URI.create(collectionUriString);
            final String repoUriString = teamParameters.get(BUILD_REPOSITORY_URI);
            final URI repoUri = URI.create(repoUriString);
            final String projectId = teamParameters.get(SYSTEM_TEAM_PROJECT);
            final String repoId = teamParameters.get(BUILD_REPOSITORY_NAME);
            final String commit = teamParameters.get(BUILD_SOURCE_VERSION);
            final String pushedBy = teamParameters.get(BUILD_REQUESTED_FOR);
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

        //noinspection UnnecessaryLocalVariable
        final Job<?, ?> job = project;
        final ParametersDefinitionProperty pp = job.getProperty(ParametersDefinitionProperty.class);
        if (pp != null) {
            final List<ParameterDefinition> parameterDefinitions = pp.getParameterDefinitions();
            final List<ParameterValue> values = new ArrayList<ParameterValue>();
            for (final ParameterDefinition d : parameterDefinitions) {
                final ParameterValue value = d.createValue(request);
                if (value != null) {
                    values.add(value);
                }
            }
            final ParametersAction action = new ParametersAction(values);
            actions.add(action);
        }

        return innerPerform(project, delay, actions);
    }
}
