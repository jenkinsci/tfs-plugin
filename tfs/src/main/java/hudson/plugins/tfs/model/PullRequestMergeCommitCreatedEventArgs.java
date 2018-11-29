//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

public class PullRequestMergeCommitCreatedEventArgs extends GitCodePushedEventArgs {
    private static final long serialVersionUID = 1L;

    public int pullRequestId;
    public int iterationId = -1;
    public String sourceCommit;
    public String targetCommit;
}
