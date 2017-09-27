//CHECKSTYLE:OFF
package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.microsoft.tfs.core.httpclient.HttpClient;
import com.microsoft.tfs.core.httpclient.NameValuePair;
import com.microsoft.tfs.util.StringUtil;
import com.microsoft.visualstudio.services.webapi.patch.Operation;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.plugins.tfs.TeamCollectionConfiguration;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.HttpMethod;
import hudson.plugins.tfs.model.JobCompletionEventArgs;
import hudson.plugins.tfs.model.JsonPatchOperation;
import hudson.plugins.tfs.model.Link;
import hudson.plugins.tfs.model.ListOfGitRepositories;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.TeamGitStatus;
import hudson.plugins.tfs.model.WorkItem;
import hudson.util.Secret;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;

public class TeamRestClient {

    private static final String AUTHORIZATION = "Authorization";
    private static final String API_VERSION = "api-version";
    private static final String NEW_LINE = System.getProperty("line.separator");

    private final URI collectionUri;
    private final boolean isTeamServices;
    private final String authorization;
    private final Server server;

    public TeamRestClient(final URI collectionUri) throws IOException {
        this(collectionUri, TeamCollectionConfiguration.findCredentialsForCollection(collectionUri));
    }

    public TeamRestClient(final URI collectionUri, final StandardUsernamePasswordCredentials credentials) throws IOException {
        this.collectionUri = collectionUri;
        final String hostName = collectionUri.getHost();
        this.server = Server.create(null, null, collectionUri.toString(), credentials, null, null);
        isTeamServices = TeamCollectionConfiguration.isTeamServices(hostName);
        if (isTeamServices && credentials != null) {
            authorization = createAuthorization(credentials);
        }
        else {
            authorization = null;
        }
    }

    public TeamRestClient(final String collectionUri, final StandardUsernamePasswordCredentials credentials) throws IOException {
        this(URI.create(collectionUri), credentials);
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
        return request(responseClass, httpMethod, requestUri, requestBody, null);
    }

    protected <TRequest, TResponse> TResponse request(
            final Class<TResponse> responseClass,
            final HttpMethod httpMethod,
            final URI requestUri,
            final TRequest requestBody,
            final NameValuePair[] additionalRequestHeaders
            ) throws IOException {

        final HttpClient httpClient = server.getHttpClient();

        final String stringRequestBody;
        if (requestBody != null) {
            final JSON jsonObject;
            if (requestBody instanceof JSON) {
                jsonObject = (JSON) requestBody;
            }
            else {
                jsonObject = JSONObject.fromObject(requestBody);
            }
            stringRequestBody = jsonObject.toString(0);
        }
        else {
            stringRequestBody = null;
        }

        final com.microsoft.tfs.core.httpclient.HttpMethod clientMethod = httpMethod.createClientMethod(requestUri.toString(), stringRequestBody);
        if (authorization != null) {
            clientMethod.addRequestHeader(AUTHORIZATION, authorization);
        }

        if (additionalRequestHeaders != null && additionalRequestHeaders.length > 0){
            for(NameValuePair pair : additionalRequestHeaders) {
                clientMethod.addRequestHeader(pair.getName(), pair.getValue());
            }
        }

        final String stringResponseBody = innerRequest(clientMethod, httpClient);

        if (responseClass == Void.class) {
            return null;
        }

        if (responseClass == String.class) {
            return (TResponse) stringResponseBody;
        }

        final TResponse result = deserialize(responseClass, stringResponseBody);
        return result;
    }

    public static <TResponse> TResponse deserialize(final Class<TResponse> responseClass, final String stringResponseBody) {
        try {
            return EndpointHelper.MAPPER.readValue(stringResponseBody, responseClass);
        }
        catch (final IOException e) {
            throw new Error(e);
        }
    }

    static String innerRequest(final com.microsoft.tfs.core.httpclient.HttpMethod clientMethod, final HttpClient httpClient) throws IOException {

        final int httpStatus = httpClient.executeMethod(clientMethod);

        final String stringResult;
        InputStream responseStream = null;
        try {
            if (httpStatus >= HttpURLConnection.HTTP_BAD_REQUEST) {
                responseStream = clientMethod.getResponseBodyAsStream();
                final String responseText = readResponseText(responseStream);
                final StringBuilder sb = new StringBuilder("HTTP ").append(httpStatus);
                final String statusText = clientMethod.getStatusText();
                if (statusText != null) {
                    sb.append(" (").append(statusText).append(")");
                }
                if (!StringUtil.isNullOrEmpty(responseText)) {
                    sb.append(": ").append(responseText);
                }
                throw new IOException(sb.toString());
            }
            responseStream = clientMethod.getResponseBodyAsStream();
            stringResult = readResponseText(responseStream);
        }
        finally {
            IOUtils.closeQuietly(responseStream);
        }
        return stringResult;
    }

    @SuppressFBWarnings(value = "DM_DEFAULT_ENCODING", justification = "Better mot modify charset in case it might raise errors")
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

    public ListOfGitRepositories getRepositories() throws IOException {
        final QueryString qs = new QueryString(API_VERSION, "1.0");
        final URI requestUri = UriHelper.join(
                collectionUri,
                "_apis",
                "git",
                "repositories",
                qs
        );

        return request(ListOfGitRepositories.class, HttpMethod.GET, requestUri, null);
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

    public WorkItem getWorkItem(final int workItemId) throws IOException {
        final QueryString qs = new QueryString(API_VERSION, "1.0");
        final URI requestUri = UriHelper.join(
                collectionUri,
                "_apis",
                "wit",
                "workitems",
                workItemId,
                qs
        );

        return request(WorkItem.class, HttpMethod.GET, requestUri, null);
    }

    public void addHyperlinkToWorkItem(final int workItemId, final String hyperlink) throws IOException {

        final JSONArray doc = new JSONArray();

        final WorkItem workItem = getWorkItem(workItemId);
        final JsonPatchOperation testRev = new JsonPatchOperation();
        testRev.setOp(Operation.TEST);
        testRev.setPath("/rev");
        testRev.setValue(workItem.getRev());
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

        // TODO: this call could fail because something else bumped the rev in the meantime; retry?
        request(Void.class, HttpMethod.PATCH, requestUri, doc);
    }

    public TeamGitStatus addPullRequestStatus(final PullRequestMergeCommitCreatedEventArgs args, final TeamGitStatus status) throws IOException {

        final QueryString qs = new QueryString(API_VERSION, "4.1-preview");
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

        final QueryString qs = new QueryString(API_VERSION, "4.1-preview");
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


    public void sendJobCompletionEvent(final JobCompletionEventArgs args) throws IOException {
        final QueryString qs = new QueryString(
                API_VERSION, "3.2",
                "publisherId", "jenkins",
                "channelId", args.getServerKey());

        final URI requestUri = UriHelper.join(
                collectionUri,
                "_apis", "public", "hooks", "externalEvents",
                qs);

        final JSONObject json = JSONObject.fromObject(args.getPayload());

        final NameValuePair[] headers = new NameValuePair[3];
        headers[0] = new NameValuePair("X-Event-Key", "job:completion");
        headers[1] = new NameValuePair("X-Jenkins-Signature", args.getPayloadSignature());
        headers[2] = new NameValuePair("X-Jenkins-ServerKey", args.getServerKey());

        request(Void.class, HttpMethod.POST, requestUri, json, headers);
    }

}
