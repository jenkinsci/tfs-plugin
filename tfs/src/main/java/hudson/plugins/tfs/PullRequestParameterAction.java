package hudson.plugins.tfs;

import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;

public class PullRequestParameterAction extends CommitParameterAction {

    private final PullRequestMergeCommitCreatedEventArgs args;

    public PullRequestParameterAction(final PullRequestMergeCommitCreatedEventArgs args) {
        super(args);
        this.args = args;
    }

    public PullRequestMergeCommitCreatedEventArgs getPullRequestMergeCommitCreatedEventArgs() {
        return args;
    }
}
