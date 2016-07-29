package hudson.plugins.tfs;

import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@link TeamBuildEndpoint}.
 */
public class TeamBuildEndpointTest {

    @Test public void decodeCommandAndJobNames_typical() throws Exception {
        final TeamBuildEndpoint cut = new TeamBuildEndpoint();
        final String input = TeamBuildEndpoint.URL_PREFIX + "ping/a";

        final boolean actual = cut.decodeCommandAndJobNames(input);

        Assert.assertEquals(true, actual);
        Assert.assertEquals("ping", cut.getCommandName());
        Assert.assertEquals("a", cut.getJobName());
    }

    @Test public void decodeCommandAndJobNames_noJob() throws Exception {
        final TeamBuildEndpoint cut = new TeamBuildEndpoint();
        final String input = TeamBuildEndpoint.URL_PREFIX + "ping/";

        final boolean actual = cut.decodeCommandAndJobNames(input);

        Assert.assertEquals(false, actual);
        Assert.assertEquals("ping", cut.getCommandName());
    }

    @Test public void decodeCommandAndJobNames_noJobNoSlash() throws Exception {
        final TeamBuildEndpoint cut = new TeamBuildEndpoint();
        final String input = TeamBuildEndpoint.URL_PREFIX + "ping";

        final boolean actual = cut.decodeCommandAndJobNames(input);

        Assert.assertEquals(false, actual);
        Assert.assertEquals("ping", cut.getCommandName());
    }

}
