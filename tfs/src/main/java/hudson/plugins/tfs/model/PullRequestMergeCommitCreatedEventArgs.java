//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

public class PullRequestMergeCommitCreatedEventArgs extends GitCodePushedEventArgs {

    public int pullRequestId;
    public int iterationId = -1;

}
