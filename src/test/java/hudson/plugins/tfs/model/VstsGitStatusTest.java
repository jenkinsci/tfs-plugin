package hudson.plugins.tfs.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link VstsGitStatus}.
 */
public class VstsGitStatusTest {

    @Test public void toJson_typical() {
        final VstsGitStatus cut = new VstsGitStatus();
        cut.state = GitStatusState.Pending;
        cut.description = "The build is in progress";
        cut.targetUrl = "https://ci.fabrikam.com/my-project/build/124";
        cut.context = new GitStatusContext("Build124", "continuous-integration");

        final String actual = cut.toJson();

        final String expected =
            "{" +
                "\"state\":\"Pending\"," +
                "\"description\":\"The build is in progress\"," +
                "\"targetUrl\":\"https://ci.fabrikam.com/my-project/build/124\"," +
                "\"context\":" +
                "{" +
                    "\"name\":\"Build124\"," +
                    "\"genre\":\"continuous-integration\"" +
                "}" +
            "}";
        Assert.assertEquals(expected, actual);
    }
}
