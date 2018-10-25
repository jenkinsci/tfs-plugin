package hudson.plugins.tfs.model;

import hudson.plugins.tfs.util.TeamRestClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link TeamGitStatus}.
 */
public class TeamGitStatusTest {

    @Test public void toJson_typical() {
        final TeamGitStatus cut = new TeamGitStatus();
        cut.state = GitStatusState.Pending;
        cut.description = "The build is in progress";
        cut.targetUrl = "https://ci.fabrikam.com/my-project/build/124";
        cut.context = new GitStatusContext("Build124", "continuous-integration");

        final String actual = cut.toJson();

        final String expected =
            "{" +
                "\"iterationId\":0," +
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

    @Test public void fromJsonString_server() throws Exception {
        final String input =
            "{" +
                "\"state\":\"succeeded\"," +
                "\"description\":\"SUCCESS\"," +
                "\"targetUrl\":\"https://ci.fabrikam.com/my-project/build/124\"," +
                "\"context\":" +
                "{" +
                    "\"name\":\"Build124\"," +
                    "\"genre\":\"continuous-integration\"" +
                "}" +
            "}";

        final TeamGitStatus actual = TeamRestClient.deserialize(TeamGitStatus.class, input);

        Assert.assertEquals(GitStatusState.Succeeded, actual.state);
    }
}
