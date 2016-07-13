package hudson.plugins.tfs;

import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.GitCodePushedEventArgsTest;
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

}
