package hudson.plugins.tfs;

import org.apache.maven.wagon.providers.http.httpclient.client.utils.URIUtils;
import org.junit.Assert;
import org.junit.runner.Description;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractIntegrationTest {

    public static final String TeamProjectCollection = "jenkins-tfs-plugin";
    public static final String TeamProjectPrefix = "$/FunctionalTests";
    public static final String TestUserName = "jenkins-tfs-plugin";
    public static final String TestUserPassword = "for-test-only";

    /**
     * Creates a string representing the URL to a default TFS server installation, based on the
     * <code>tfs_server_name</code> property.
     *
     * @return a string of the form <code>http://${tfs_server_name}:8080/tfs</code>
     * @throws URISyntaxException
     */
    public static String buildTfsServerUrl() throws URISyntaxException {
        final String tfs_server_name = getTfsServerName();
        Assert.assertNotNull("The 'tfs_server_name' property was not provided a [non-empty] value.", tfs_server_name);
        final URI serverUri = URIUtils.createURI("http", tfs_server_name, 8080, "tfs", null, null);
        return serverUri.toString();
    }

    public static String getTfsServerName() {
        return hudson.Util.fixEmptyAndTrim(System.getProperty("tfs_server_name"));
    }

    /**
     * Creates a string representing a path in TFVC where the specified {@param testDescription}
     * will perform its work.
     *
     * @param testDescription metadata about the currently executing test method.
     * @return a string that looks like <code>$/FunctionalTests/TestClass/testMethod</code>
     */
    public static String determinePathInTfvcForTestCase(Description testDescription) {
        final Class clazz = testDescription.getTestClass();
        final String testClassName = clazz.getSimpleName();
        final String testCaseName = testDescription.getMethodName();
        return TeamProjectPrefix + "/" + testClassName + "/" + testCaseName;
    }
}
