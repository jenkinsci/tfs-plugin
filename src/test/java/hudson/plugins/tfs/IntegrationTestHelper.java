package hudson.plugins.tfs;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.wagon.providers.http.httpclient.client.utils.URIUtils;
import org.junit.Assert;
import org.junit.runner.Description;

import javax.xml.bind.DatatypeConverter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class IntegrationTestHelper {

    public static final String TeamProjectCollection = "jenkins-tfs-plugin";
    public static final String TeamProjectPrefix = "$/FunctionalTests";
    public static final String TestUserName = "jenkins-tfs-plugin";
    public static final String TestUserPassword = "for-test-only";

    private final String serverUrl;

    public IntegrationTestHelper() throws URISyntaxException {
        serverUrl = buildTfsServerUrl();
    }

    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Creates a string representing the URL to a VSO account or a default TFS server installation,
     * based on the <code>tfs_server_name</code> property.
     *
     * @return a string representing a URL to a VSO or TFS server.
     * @throws URISyntaxException
     */
    public static String buildTfsServerUrl() throws URISyntaxException {
        final String tfs_server_name = getTfsServerName();
        Assert.assertNotNull("The 'tfs_server_name' property was not provided a [non-empty] value.", tfs_server_name);
        return buildServerUrl(tfs_server_name);
    }

    public static String buildServerUrl(final String tfs_server_name) throws URISyntaxException {
        final URI serverUri;
        if ("vso".equals(tfs_server_name)) {
            serverUri = URIUtils.createURI("https", "automated-testing.visualstudio.com", 443, "DefaultCollection", null, null);
        } else {
            serverUri = URIUtils.createURI("http", tfs_server_name, 8080, "tfs/" + TeamProjectCollection, null, null);
        }
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

    // Adapted from http://stackoverflow.com/a/20793241
    public static String tryToDetermineHostName() {
        String result;
        try {
            result = InetAddress.getLocalHost().getHostName();
            if (StringUtils.isNotEmpty(result)) {
                return result;
            }
        } catch (UnknownHostException e) {
            // Probably failed due to reasons listed here: http://stackoverflow.com/a/7800008
        }

        result = System.getenv("COMPUTERNAME");
        if (result != null) {
            return result;
        }

        result = System.getenv("HOSTNAME");
        if (result != null) {
            return result;
        }

        result = inventHostName();
        if (result == null) {
            result = "unknown";
        }
        return result;
    }

    // Adapted from http://stackoverflow.com/q/8765578
    public static String inventHostName() {
        String result = null;
        String ipv4Address = null, ipv6Address = null;
        try {
            final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (ipv6Address == null && interfaces.hasMoreElements()) {
                final NetworkInterface current = interfaces.nextElement();
                try {
                    if (!current.isUp() || current.isLoopback() || current.isVirtual()) {
                        continue;
                    }
                } catch (SocketException e) {
                    continue;
                }
                final Enumeration<InetAddress> addresses = current.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    final InetAddress currentAddress = addresses.nextElement();
                    if (currentAddress.isLoopbackAddress()) {
                        continue;
                    }
                    final byte[] addressBytes = currentAddress.getAddress();
                    if (currentAddress instanceof Inet6Address) {
                        ipv6Address = formatFriendlyName(addressBytes);
                        break;
                    }
                    if (currentAddress instanceof Inet4Address) {
                        ipv4Address = formatFriendlyName(addressBytes);
                    }
                }
            }
            if (ipv6Address != null) {
                result = ipv6Address;
            }
            else if (ipv4Address != null) {
                result = ipv4Address;
            }
        } catch (SocketException e) {
            // result will stay null
        }
        return result;
    }

    public static String formatFriendlyName(final byte[] addressBytes) {
        final String base64 = DatatypeConverter.printBase64Binary(addressBytes);
        final String slashToMinus = base64.replace('/', '-');
        return slashToMinus;
    }
}
