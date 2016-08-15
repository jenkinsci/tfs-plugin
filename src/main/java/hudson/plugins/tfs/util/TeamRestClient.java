package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.plugins.tfs.TeamCollectionConfiguration;
import com.microsoft.visualstudio.services.webapi.patch.Operation;
import com.microsoft.visualstudio.services.webapi.patch.json.JsonPatchDocument;
import com.microsoft.visualstudio.services.webapi.patch.json.JsonPatchOperation;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.HttpMethod;
import hudson.plugins.tfs.model.Link;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;
import hudson.plugins.tfs.model.TeamGitStatus;
import hudson.util.Secret;
import net.sf.json.JSONObject;
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

public class TeamRestClient {

    private static final String AUTHORIZATION = "Authorization";
    private static final String API_VERSION = "api-version";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private final URI collectionUri;
    private final boolean isTeamServices;
    private final String authorization;

    public TeamRestClient(final URI collectionUri) {
        this.collectionUri = collectionUri;
        final String hostName = collectionUri.getHost();
        isTeamServices = TeamCollectionConfiguration.isTeamServices(hostName);
        final StandardUsernamePasswordCredentials credentials = TeamCollectionConfiguration.findCredentialsForCollection(collectionUri);
        if (credentials != null) {
            authorization = createAuthorization(credentials);
        }
        else {
            authorization = null;
        }
    }

    public TeamRestClient(final URI collectionUri, final StandardUsernamePasswordCredentials credentials) {
        this.collectionUri = collectionUri;
        final String hostName = collectionUri.getHost();
        isTeamServices = TeamCollectionConfiguration.isTeamServices(hostName);
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

            final TResponse result = deserialize(responseClass, stringResponseBody);
            return result;
        }
        finally {
            connection.disconnect();
        }
    }

    public static <TResponse> TResponse deserialize(final Class<TResponse> responseClass, final String stringResponseBody) {
        try {
            return MAPPER.readValue(stringResponseBody, responseClass);
        }
        catch (final IOException e) {
            throw new Error(e);
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
        if (isTeamServices) {
            requestUri = UriHelper.join(collectionUri, "_apis", "connectiondata");
        }
        else {
            requestUri = UriHelper.join(collectionUri, "_home", "About");
        }

        return request(String.class, HttpMethod.GET, requestUri, null);
    }

    public TeamGitStatus addCommitStatus(final GitCodePushedEventArgs args, final TeamGitStatus status) throws IOException {

        final QueryString qs = new QueryString(API_VERSION, "2.1");
        final URI requestUri = UriHelper.join(
            collectionUri, args.projectId,
            "_apis", "git",
            "repositories", args.repoId,
            "commits", args.commit,
            "statuses",
            qs);

        return request(TeamGitStatus.class, HttpMethod.POST, requestUri, status);
    }

    public void addHyperlinkToWorkItem(final int workItemId, final String hyperlink) throws IOException {
        final JsonPatchDocument doc = new JsonPatchDocument();

        final JsonPatchOperation testRev = new JsonPatchOperation();
        testRev.setOp(Operation.TEST);
        testRev.setPath("/rev");
        testRev.setValue(workItemId);
        doc.add(testRev);

        // TODO: do we also need to "add" to "/fields/System.History"?

        final Link link = new Link("Hyperlink", hyperlink);
        final JsonPatchOperation addRelation = new JsonPatchOperation();
        addRelation.setOp(Operation.ADD);
        addRelation.setPath("/relations/-");
        addRelation.setValue(link);
        doc.add(addRelation);

        final QueryString qs = new QueryString(API_VERSION, "1.0");
        final URI requestUri = UriHelper.join(
            collectionUri,
            "_apis",
            "wit",
            "workitems",
            workItemId,
            qs);

        request(Void.class, HttpMethod.PATCH, requestUri, doc);
    }

    public TeamGitStatus addPullRequestStatus(final PullRequestMergeCommitCreatedEventArgs args, final TeamGitStatus status) throws IOException {

        final QueryString qs = new QueryString(API_VERSION, "3.0-preview.1");
        final URI requestUri = UriHelper.join(
            collectionUri, args.projectId,
            "_apis", "git",
            "repositories", args.repoId,
            "pullRequests", args.pullRequestId,
            "statuses",
            qs);

        return request(TeamGitStatus.class, HttpMethod.POST, requestUri, status);
    }

    public TeamGitStatus addPullRequestIterationStatus(final PullRequestMergeCommitCreatedEventArgs args, final TeamGitStatus status) throws IOException {

        final QueryString qs = new QueryString(API_VERSION, "3.0-preview.1");
        final URI requestUri = UriHelper.join(
            collectionUri, args.projectId,
            "_apis", "git",
            "repositories", args.repoId,
            "pullRequests", args.pullRequestId,
            "iterations", args.iterationId,
            "statuses",
            qs);

        return request(TeamGitStatus.class, HttpMethod.POST, requestUri, status);
    }
}
