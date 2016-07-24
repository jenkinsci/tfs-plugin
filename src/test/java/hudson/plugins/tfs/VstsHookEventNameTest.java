package hudson.plugins.tfs;

import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.GitCodePushedEventArgsTest;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgsTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link VstsHookEventName}.
 */
public class VstsHookEventNameTest {

    @Test public void gitCodePushed() throws Exception {
        final String input = GitCodePushedEventArgsTest.FORMATTED_INPUT;

        final Object actual = VstsHookEventName.GIT_CODE_PUSHED.parse(input);

        Assert.assertTrue(actual instanceof GitCodePushedEventArgs);
    }

    @Test public void pullRequestMergeCommitCreated() throws Exception {
        final String input = PullRequestMergeCommitCreatedEventArgsTest.FORMATTED_INPUT;

        final Object actual = VstsHookEventName.PULL_REQUEST_MERGE_COMMIT_CREATED.parse(input);

        Assert.assertTrue(actual instanceof PullRequestMergeCommitCreatedEventArgs);
    }
}
