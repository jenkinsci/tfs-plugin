package hudson.plugins.tfs.model;

import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.PullRequestParameterAction;
import net.sf.json.JSONObject;

import java.util.List;

public class PullRequestMergeCommitCreatedHookEvent extends GitCodePushedHookEvent {

    public static class Factory implements AbstractHookEvent.Factory {
        @Override
        public AbstractHookEvent create() {
            return new PullRequestMergeCommitCreatedHookEvent();
        }

        @Override
        public String getSampleRequestPayload() {
            return PullRequestMergeCommitCreatedEventArgs.SAMPLE_REQUEST_PAYLOAD;
        }
    }

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        final PullRequestMergeCommitCreatedEventArgs args = PullRequestMergeCommitCreatedEventArgs.fromJsonObject(requestPayload);
        final PullRequestParameterAction parameterAction = new PullRequestParameterAction(args);
        final List<GitStatus.ResponseContributor> contributors = pollOrQueueFromEvent(args, parameterAction, true);
        final JSONObject response = fromResponseContributors(contributors);
        return response;
    }
}
