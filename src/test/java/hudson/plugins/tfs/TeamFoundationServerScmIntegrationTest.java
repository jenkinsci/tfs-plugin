package hudson.plugins.tfs;

import hudson.util.Secret;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URISyntaxException;

/**
 * Tests that connect to a TFS server identified by the tfs_server_name property.
 * These are so-called integration (L2) tests.
 */
@Category(IntegrationTests.class)
public class TeamFoundationServerScmIntegrationTest {

    private TeamFoundationServerScm scm;

    @Before
    public void connectToTfs() throws URISyntaxException {
        final IntegrationTestHelper helper = new IntegrationTestHelper();
        final String serverUrl = helper.getServerUrl();
        scm = new TeamFoundationServerScm(serverUrl, "projectPath", "workspaceName", null, null);
        scm.setLocalPath("localPath");
    }

    @Test
    public void sample() {
        Assert.assertNotNull(scm);
    }
}
