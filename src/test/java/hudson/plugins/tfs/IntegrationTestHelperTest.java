package hudson.plugins.tfs;

import org.junit.Assert;
import org.junit.Test;

public class IntegrationTestHelperTest {

    @Test public void buildServerUrl_onPremiseTfsServer() throws Exception {
        final IntegrationTestHelper cut = new IntegrationTestHelper("tfs2013");

        final String actual = cut.getServerUrl();

        Assert.assertEquals("http://tfs2013:8080/tfs/jenkins-tfs-plugin", actual);
    }

    @Test public void buildServerUrl_designatedVsoAccount() throws Exception {
        final IntegrationTestHelper cut = new IntegrationTestHelper("automated-testing.visualstudio.com");

        final String actual = cut.getServerUrl();

        Assert.assertEquals("https://automated-testing.visualstudio.com:443/DefaultCollection", actual);
    }

}
