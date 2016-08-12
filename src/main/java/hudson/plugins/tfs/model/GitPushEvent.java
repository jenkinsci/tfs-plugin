package hudson.plugins.tfs.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.teamfoundation.core.webapi.model.TeamProjectReference;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitCommitRef;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPush;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import com.microsoft.visualstudio.services.webapi.model.IdentityRef;
import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.CommitParameterAction;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class GitPushEvent extends AbstractHookEvent {

    static final String EVENT_TYPE = "eventType";
    static final String RESOURCE = "resource";
    static final String REPOSITORY = "repository";
    static final String REMOTE_URL = "remoteUrl";
    static final String NAME = "name";

    private static final String GIT_PUSH = "git.push";

    public static class Factory implements AbstractHookEvent.Factory {

        @Override
        public AbstractHookEvent create() {
            return new GitPushEvent();
        }

        @Override
        public String getSampleRequestPayload() {
            return fetchResourceAsString(this.getClass(), "GitPushEvent.json");
        }
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final JsonParser resourceParser) {
        final GitPush gitPush;
        try {
            gitPush = mapper.readValue(resourceParser, GitPush.class);
        }
        catch (final IOException e) {
            throw new Error(e);
        }

        final GitCodePushedEventArgs args = decodeGitPush(gitPush);
        final CommitParameterAction parameterAction = new CommitParameterAction(args);
        final List<GitStatus.ResponseContributor> contributors = pollOrQueueFromEvent(args, parameterAction, false);
        final JSONObject response = fromResponseContributors(contributors);
        return response;
    }

    static void assertEquals(final JSONObject jsonObject, final String key, final String expectedValue) {
        final String template = "Expected key '%s' to be equal to %s in object:\n%s\n";
        final String message = String.format(template, key, expectedValue, jsonObject);
        if (!jsonObject.containsKey(key)) {
            throw new IllegalArgumentException(message);
        }
        final Object actualValue = jsonObject.get(key);
        if (actualValue instanceof String) {
            final String actualStringValue = (String) actualValue;
            if (!expectedValue.equals(actualStringValue)) {
                throw new IllegalArgumentException(message);
            }
        }
        else {
            throw new IllegalArgumentException(message);
        }
    }

    static URI determineCollectionUri(final URI repoApiUri) {
        final String path = repoApiUri.getPath();
        final int i = path.indexOf("_apis/");
        if (i == -1) {
            final String template = "Repository url '%s' did not contain '_apis/'.";
            throw new IllegalArgumentException(String.format(template, repoApiUri));
        }
        final String pathBeforeApis = path.substring(0, i);
        final URI uri;
        try {
            uri = new URI(repoApiUri.getScheme(), repoApiUri.getAuthority(), pathBeforeApis, repoApiUri.getQuery(), repoApiUri.getFragment());
        }
        catch (final URISyntaxException e) {
            throw new Error(e);
        }
        return uri;
    }

    static URI determineCollectionUri(final JSONObject repository) {
        final String repoApiUrlString = repository.getString("url");
        final URI repoApiUri = URI.create(repoApiUrlString);
        return determineCollectionUri(repoApiUri);
    }

    static URI determineCollectionUri(final GitRepository repository) {
        final String repoApiUrlString = repository.getUrl();
        final URI repoApiUri = URI.create(repoApiUrlString);
        return determineCollectionUri(repoApiUri);
    }

    static String determineProjectId(final JSONObject repository) {
        final JSONObject project = repository.getJSONObject("project");
        final String result = project.getString("name");
        return result;
    }

    static String determineProjectId(final GitRepository repository) {
        final TeamProjectReference project = repository.getProject();
        final String result = project.getName();
        return result;
    }

    static String determineCommit(final JSONObject resource) {
        final JSONArray commits = resource.getJSONArray("commits");
        if (commits.size() < 1) {
            throw new IllegalArgumentException("No commits found");
        }
        final JSONObject commitObject = commits.getJSONObject(0);
        return commitObject.getString("commitId");
    }

    static String determineCommit(final GitPush gitPush) {
        final List<GitCommitRef> commits = gitPush.getCommits();
        if (commits.size() < 1) {
            throw new IllegalArgumentException("No commits found");
        }
        final GitCommitRef commit = commits.get(0);
        return commit.getCommitId();
    }

    static String determinePushedBy(final JSONObject resource) {
        final JSONObject pushedBy = resource.getJSONObject("pushedBy");
        final String result = pushedBy.getString("displayName");
        return result;
    }

    static String determinePushedBy(final GitPush gitPush) {
        final IdentityRef pushedBy = gitPush.getPushedBy();
        final String result = pushedBy.getDisplayName();
        return result;
    }

    static GitCodePushedEventArgs decodeGitPush(final JSONObject gitPushJson) {
        assertEquals(gitPushJson, EVENT_TYPE, GIT_PUSH);
        final JSONObject resource = gitPushJson.getJSONObject(RESOURCE);
        final JSONObject repository = resource.getJSONObject(REPOSITORY);
        final URI collectionUri = determineCollectionUri(repository);
        final String repoUriString = repository.getString(REMOTE_URL);
        final URI repoUri = URI.create(repoUriString);
        final String projectId = determineProjectId(repository);
        final String repoId = repository.getString(NAME);
        final String commit = determineCommit(resource);
        final String pushedBy = determinePushedBy(resource);

        final GitCodePushedEventArgs args = new GitCodePushedEventArgs();
        args.collectionUri = collectionUri;
        args.repoUri = repoUri;
        args.projectId = projectId;
        args.repoId = repoId;
        args.commit = commit;
        args.pushedBy = pushedBy;
        return args;
    }

    static GitCodePushedEventArgs decodeGitPush(final GitPush gitPush) {
        final GitRepository repository = gitPush.getRepository();
        final URI collectionUri = determineCollectionUri(repository);
        final String repoUriString = repository.getRemoteUrl();
        final URI repoUri = URI.create(repoUriString);
        final String projectId = determineProjectId(repository);
        final String repoId = repository.getName();
        final String commit = determineCommit(gitPush);
        final String pushedBy = determinePushedBy(gitPush);

        final GitCodePushedEventArgs args = new GitCodePushedEventArgs();
        args.collectionUri = collectionUri;
        args.repoUri = repoUri;
        args.projectId = projectId;
        args.repoId = repoId;
        args.commit = commit;
        args.pushedBy = pushedBy;
        return args;
    }
}
