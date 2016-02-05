package hudson.plugins.tfs;

import hudson.*;
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

    private final String serverUrl;
    private final String userName;
    private final String userPassword;

    public IntegrationTestHelper() throws URISyntaxException {
        this(getTfsServerName());
    }

    public IntegrationTestHelper(final String tfsServerName) throws URISyntaxException {
        final URI serverUri;
        final String tfsUserName = propertyOrNull("tfs_user_name");
        final String tfsUserPassword = propertyOrNull("tfs_user_password");
        if (tfsServerName.endsWith(".visualstudio.com")) {
            serverUri = URIUtils.createURI("https", tfsServerName, 443, "DefaultCollection", null, null);
            this.userName = tfsUserName;
            this.userPassword = tfsUserPassword;
        } else {
            serverUri = URIUtils.createURI("http", tfsServerName, 8080, "tfs/" + "jenkins-tfs-plugin", null, null);
            this.userName = (tfsUserName != null) ? tfsUserName : "jenkins-tfs-plugin";
            this.userPassword = (tfsUserPassword != null) ? tfsUserPassword : "for-test-only";
        }
        serverUrl = serverUri.toString();
    }

    static String propertyOrNull(final String propertyName) {
        final String value = System.getProperty(propertyName);
        return hudson.Util.fixEmptyAndTrim(value);
    }

    /**
     * A string representing the URL to a VSO account or a default TFS server installation.
     */
    public String getServerUrl() {
        return serverUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public static String getTfsServerName() {
        final String result = propertyOrNull("tfs_server_name");
        Assert.assertNotNull("The 'tfs_server_name' property was not provided a [non-empty] value.", result);
        return result;
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
        return "$/FunctionalTests" + "/" + testClassName + "/" + testCaseName;
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
