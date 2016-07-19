package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.plugins.tfs.model.HttpMethod;
import hudson.util.Secret;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

public class VstsRestClient {

    private static final String AUTHORIZATION = "Authorization";
    private static final String NEW_LINE = System.getProperty("line.separator");

    private final URI collectionUri;
    private final boolean isHosted;
    private final String authorization;

    public VstsRestClient(final URI collectionUri, final StandardUsernamePasswordCredentials credentials) {
        this.collectionUri = collectionUri;
        final String hostName = collectionUri.getHost();
        isHosted = StringHelper.endsWithIgnoreCase(hostName, ".visualstudio.com");
        if (credentials != null) {
            authorization = createAuthorization(credentials);
        }
        else {
            authorization = null;
        }
    }

    static String createAuthorization(final StandardUsernamePasswordCredentials credentials) {
        final String username = credentials.getUsername();
        final Secret secretPassword = credentials.getPassword();
        final String password = secretPassword.getPlainText();
        final String credPair = username + ":" + password;
        final byte[] credBytes = credPair.getBytes(MediaType.UTF_8);
        final String base64enc = DatatypeConverter.printBase64Binary(credBytes);
        final String result = "Basic " + base64enc;
        return result;
    }

    protected <TRequest, TResponse> TResponse request(
        final Class<TResponse> responseClass,
        final HttpMethod httpMethod,
        final URI requestUri,
        final TRequest requestBody
        ) throws IOException {

        final URL requestUrl;
        try {
            requestUrl = requestUri.toURL();
        }
        catch (final MalformedURLException e) {
            throw new Error(e);
        }
        // TODO: support Jenkins' proxy server configuration
        final HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
        try {
            if (authorization != null) {
                connection.setRequestProperty(AUTHORIZATION, authorization);
            }
            // TODO: add User-Agent

            final String stringRequestBody;
            if (requestBody != null) {
                final JSONObject jsonObject = JSONObject.fromObject(requestBody);
                stringRequestBody = jsonObject.toString();
            }
            else {
                stringRequestBody = null;
            }

            final String stringResponseBody = innerRequest(httpMethod, connection, stringRequestBody);

            if (responseClass == Void.class) {
                return null;
            }

            if (responseClass == String.class) {
                return (TResponse) stringResponseBody;
            }

            final JSONTokener tokener = new JSONTokener(stringResponseBody);
            final JSONObject jsonObject = JSONObject.fromObject(tokener);
            final TResponse result = (TResponse) jsonObject.toBean(responseClass);
            return result;
        }
        finally {
            connection.disconnect();
        }
    }

    static String innerRequest(final HttpMethod httpMethod, final HttpURLConnection connection, final String body) throws IOException {
        httpMethod.sendRequest(connection, body);

        final int httpStatus = connection.getResponseCode();
        final String stringResult;
        InputStream responseStream = null;
        try {
            if (httpStatus >= HttpURLConnection.HTTP_BAD_REQUEST) {
                responseStream = connection.getErrorStream();
                if (responseStream == null) {
                    responseStream = connection.getInputStream();
                }
                final String responseText = readResponseText(responseStream);
                throw new Error(responseText);
            }
            responseStream = connection.getInputStream();
            stringResult = readResponseText(responseStream);
        }
        finally {
            IOUtils.closeQuietly(responseStream);
        }
        return stringResult;
    }

    static String readResponseText(final InputStream inputStream) throws IOException {
        final InputStreamReader isr = new InputStreamReader(inputStream);
        final BufferedReader reader = new BufferedReader(isr);
        final StringBuilder sb = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(NEW_LINE);
            }
        }
        finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(inputStream);
        }
        return sb.toString();
    }

    public String ping() throws IOException {
        final URI requestUri;
        if (isHosted) {
            requestUri = UriHelper.join(collectionUri, "_apis", "connectiondata");
        }
        else {
            requestUri = UriHelper.join(collectionUri, "_home", "About");
        }

        return request(String.class, HttpMethod.GET, requestUri, null);
    }

}
