package hudson.plugins.tfs;

import hudson.plugins.tfs.model.ConnectionParameters;
import hudson.plugins.tfs.model.JobCompletionEventArgs;
import hudson.plugins.tfs.util.TeamRestClient;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class sends Jenkins events to all "connected" team collections.
 * Current list of events:
 * - Job Completion event
 */
public final class JenkinsEventNotifier {
    private static final Logger log = Logger.getLogger(JenkinsEventNotifier.class.getName());
    private static final String ENCODING = "UTF-8";

    /**
     * Hiding the constructor for this Utility class.
     */
    private JenkinsEventNotifier() { }

    /**
     * Send the Job Completion event to connected TFS/VSTS servers.
     */
    public static void sendJobCompletionEvent(final JSONObject payload) {
        final List<TeamCollectionConfiguration> connectedCollections = TeamCollectionConfiguration.getConnectedCollections();
        for (final TeamCollectionConfiguration c : connectedCollections) {
            try {
                // Check to see if there are any collections "connected" to this Jenkins server
                final ConnectionParameters connectionParameters = c.getConnectionParameters();
                final TeamRestClient client = new TeamRestClient(URI.create(c.getCollectionUrl()));
                payload.put("server", connectionParameters.getConnectionKey());
                final String jsonPayload = payload.toString();
                final JobCompletionEventArgs args = new JobCompletionEventArgs(
                        connectionParameters.getConnectionKey(),
                        jsonPayload,
                        getPayloadSignature(connectionParameters.getConnectionSignature(), jsonPayload));
                client.sendJobCompletionEvent(args);
            } catch (final Exception e) {
                log.warning("ERROR: sendJobCompletionEvent: (collection=" + c.getCollectionUrl() + ") " + e.getMessage());
            }
        }
    }

    /**
     * This is a helper method to get the JSON for a Jenkins object.
     * @param url
     * @return
     */
    public static String getApiJson(final String url) {
        try {
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                return "";
            }
            final String rootUrl = jenkins.getRootUrl();
            final String fullUrl = urlCombine(rootUrl, url, "api", "json");
            final HttpClient client = HttpClientBuilder.create().build();
            final HttpGet request = new HttpGet(fullUrl);

            // add request header
            request.addHeader("User-Agent", "Jenkins-Self");

            final HttpResponse response = client.execute(request);
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode <= HttpURLConnection.HTTP_ACCEPTED) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), ENCODING))) {
                    final StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                        result.append("\n");
                    }
                    return result.toString();
                }
            } else {
                log.warning("ERROR: getApiJson: (url=" + url + ") failed due to Http error #" + statusCode);
                return null;
            }
        } catch (final HttpHostConnectException e) {
            log.warning("ERROR: getApiJson: (url=" + url + ") " + e.getMessage());
            return null;
        } catch (final Exception e) {
            log.warning("ERROR: getApiJson: (url=" + url + ") " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Calculates the payload hash.
     * @param secret
     * @param payload
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     */
    public static String getPayloadSignature(final String secret, final String payload)
            throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
        final SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(ENCODING), "HmacSHA1");
        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        return toHexString(mac.doFinal(payload.getBytes(ENCODING)));
    }

    private static String urlCombine(final String url, final String... parts) {
        final StringBuilder sb = new StringBuilder();
        if (url != null) {
            sb.append(url);
            for (final String s : parts) {
                if (StringUtils.isNotBlank(s)) {
                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
                        sb.append("/");
                    }
                    sb.append(s);
                }
            }
        }
        return sb.toString();
    }

    private static String toHexString(final byte[] bytes) {
        final Formatter formatter = new Formatter();
        for (final byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }
}
