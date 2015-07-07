package hudson.plugins.tfs;

import org.apache.maven.wagon.providers.http.httpclient.client.utils.URIUtils;
import org.junit.Assert;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractIntegrationTest {
    /**
     * Creates a string representing the URL to a default TFS server installation, based on the
     * <code>tfs_server_name</code> property.
     *
     * @return a string of the form <code>http://${tfs_server_name}:8080/tfs</code>
     * @throws URISyntaxException
     */
    public static String buildTfsServerUrl() throws URISyntaxException {
        final String tfs_server_name = hudson.Util.fixEmptyAndTrim(System.getProperty("tfs_server_name"));
        Assert.assertNotNull("The 'tfs_server_name' property was not provided a [non-empty] value.", tfs_server_name);
        final URI serverUri = URIUtils.createURI("http", tfs_server_name, 8080, "tfs", null, null);
        return serverUri.toString();
    }
}
