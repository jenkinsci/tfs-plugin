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
import org.apache.http.impl.client.HttpClientBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class JenkinsEventNotifier {
    protected final static Logger log = Logger.getLogger(JenkinsEventNotifier.class.getName());

    /**
     * Send the Job Completion event to connected TFS/VSTS servers
     */
    public static void sendJobCompletionEvent(final JSONObject payload) {
        final List<TeamCollectionConfiguration> connectedCollections = TeamCollectionConfiguration.getConnectedCollections();
        for (final TeamCollectionConfiguration c : connectedCollections) {
            try {
                // Check to see if there are any collections "connected" to this Jenkins server
                final ConnectionParameters connectionParameters = c.getConnectionParameters();
                if (connectionParameters == null || !connectionParameters.isSendJobCompletionEvents()) {
                    // This server isn't accepting events
                    continue;
                }
                final TeamRestClient client = new TeamRestClient(URI.create(c.getCollectionUrl()));
                payload.put("server", connectionParameters.getConnectionKey());
                final String jsonPayload = payload.toString();
                final JobCompletionEventArgs args = new JobCompletionEventArgs(
                        connectionParameters.getConnectionKey(), jsonPayload,
                        getPayloadSignature(connectionParameters, jsonPayload));
                client.sendJobCompletionEvent(args);
            } catch (final Exception e) {
                log.info("ERROR: sendJobCompletionEvent: (collection=" + c.getCollectionUrl() + ") " + e.getMessage());
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
            final String rootUrl = Jenkins.getInstance().getRootUrl();
            final String fullUrl = urlCombine(rootUrl, url, "api", "json");
            final HttpClient client = HttpClientBuilder.create().build();
            final HttpGet request = new HttpGet(fullUrl);

            // add request header
            request.addHeader("User-Agent", "Jenkins-Self");

            final HttpResponse response = client.execute(request);

            final BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            final StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            return result.toString();
        } catch (final IOException e) {
            log.info("ERROR: getApiJson: (url=" + url + ") " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static String urlCombine(final String url, final String... parts) {
        final StringBuilder sb = new StringBuilder();
        sb.append(url);
        for(final String s : parts) {
            if (StringUtils.isNotBlank(s)) {
                if (sb.charAt(sb.length() - 1) != '/') {
                    sb.append("/");
                }
                sb.append(s);
            }
        }
        return sb.toString();
    }

    private static String getPayloadSignature(final ConnectionParameters connectionParameters, final String payload)
            throws NoSuchAlgorithmException, InvalidKeyException {
        final String key = connectionParameters.getConnectionSignature();
        final SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signingKey);
        return toHexString(mac.doFinal(payload.getBytes()));
    }

    private static String toHexString(final byte[] bytes) {
        final Formatter formatter = new Formatter();
        for (final byte b : bytes) {
            formatter.format("%02x", b);
        }

        return formatter.toString();
    }


}
