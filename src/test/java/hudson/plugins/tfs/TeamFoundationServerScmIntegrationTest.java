package hudson.plugins.tfs;

import hudson.Util;
import hudson.util.Secret;
import org.apache.http.client.utils.URIUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.URI;
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
        final String serverUrl = buildTfsServerUrl();
        scm = new TeamFoundationServerScm(serverUrl, "projectPath", "localPath", false, "workspaceName", null, (Secret) null);
    }

    /**
     * Creates a string representing the URL to a default TFS server installation, based on the
     * <code>tfs_server_name</code> property.
     *
     * @return a string of the form <code>http://${tfs_server_name}:8080/tfs</code>
     * @throws URISyntaxException
     */
    public String buildTfsServerUrl() throws URISyntaxException {
        final String tfs_server_name = Util.fixEmptyAndTrim(System.getProperty("tfs_server_name"));
        Assert.assertNotNull("The 'tfs_server_name' property was not provided a [non-empty] value.", tfs_server_name);
        final URI serverUri = URIUtils.createURI("http", tfs_server_name, 8080, "tfs", null, null);
        return serverUri.toString();
    }

    @Test
    public void sample() {
        Assert.assertNotNull(scm);
    }
}
