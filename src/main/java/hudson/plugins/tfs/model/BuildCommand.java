package hudson.plugins.tfs.model;

import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Queue;
import hudson.model.queue.ScheduleResult;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.PullRequestParameterAction;
import jenkins.model.Jenkins;
import jenkins.util.TimeDuration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.ArrayList;
import java.util.List;

public class BuildCommand extends AbstractCommand {

    private static final String TEAM_PARAMETERS = "team-parameters";
    private static final Action[] EMPTY_ACTION_ARRAY = new Action[0];

    public static class Factory implements AbstractCommand.Factory {
        @Override
        public AbstractCommand create() {
            return new BuildCommand();
        }

        @Override
        public String getSampleRequestPayload() {
            return "{\n" +
                    "    \"team-parameters\":\n" +
                    "    {\n" +
                    "        \"collectionUri\":\"https://fabrikam-fiber-inc.visualstudio.com\",\n" +
                    "        \"repoUri\":\"https://fabrikam-fiber-inc.visualstudio.com/Personal/_git/olivida.tfs-plugin\",\n" +
                    "        \"projectId\":\"Personal\",\n" +
                    "        \"repoId\":\"olivida.tfs-plugin\",\n" +
                    "        \"commit\":\"6a23fc7afec31f0a14bade6544bed4f16492e6d2\",\n" +
                    "        \"pushedBy\":\"olivida\"\n" +
                    "    }\n" +
                    "}";
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
    public JSONObject perform(final AbstractProject project, final JSONObject requestPayload, final TimeDuration delay) {

        final List<Action> actions = new ArrayList<Action>();
        if (requestPayload.containsKey(TEAM_PARAMETERS)) {
            final JSONObject eventArgsJson = requestPayload.getJSONObject(TEAM_PARAMETERS);
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
        // TODO: detect if a job is parameterized and react appropriately

        return innerPerform(project, delay, actions);
    }

    @Override
    public JSONObject perform(final AbstractProject project, final StaplerRequest request, final TimeDuration delay) {
        throw new IllegalStateException("Not implemented");
    }
}
