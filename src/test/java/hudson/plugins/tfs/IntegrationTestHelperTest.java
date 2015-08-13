package hudson.plugins.tfs;

import org.junit.Assert;
import org.junit.Test;

public class IntegrationTestHelperTest {

    @Test public void buildServerUrl_onPremiseTfsServer() throws Exception {
        final String actual = IntegrationTestHelper.buildServerUrl("tfs2013");

        Assert.assertEquals("http://tfs2013:8080/tfs/jenkins-tfs-plugin", actual);
    }

}
