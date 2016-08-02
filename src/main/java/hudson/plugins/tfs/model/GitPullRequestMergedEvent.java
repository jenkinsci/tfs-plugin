package hudson.plugins.tfs.model;

import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.util.MediaType;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

public class GitPullRequestMergedEvent extends GitPushEvent {

    private static final String GIT_PULLREQUEST_MERGED = "git.pullrequest.merged";

    public static class Factory implements AbstractHookEvent.Factory {

        @Override
        public AbstractHookEvent create(final JSONObject requestPayload) {
            return new GitPullRequestMergedEvent(requestPayload);
        }

        @Override
        public String getSampleRequestPayload() {
            final Class<? extends Factory> me = this.getClass();
            final InputStream stream = me.getResourceAsStream("GitPullRequestMergedEvent.json");
            try {
                return IOUtils.toString(stream, MediaType.UTF_8);
            }
            catch (final IOException e) {
                throw new Error(e);
            }
            finally {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    public GitPullRequestMergedEvent(final JSONObject requestPayload) {
        super(requestPayload);
    }

    static String determineCreatedBy(final JSONObject resource) {
        final JSONObject createdBy = resource.getJSONObject("createdBy");
        final String result = createdBy.getString("displayName");
        return result;
    }

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        final PullRequestMergeCommitCreatedEventArgs args = decodeGitPullRequestMerged(requestPayload);
        final PullRequestParameterAction parameterAction = new PullRequestParameterAction(args);
        final List<GitStatus.ResponseContributor> contributors = pollOrQueueFromEvent(args, parameterAction, false);
        final JSONObject response = fromResponseContributors(contributors);
        return response;
    }

    static PullRequestMergeCommitCreatedEventArgs decodeGitPullRequestMerged(final JSONObject gitPullRequestMergedJson) {
        assertEquals(gitPullRequestMergedJson, EVENT_TYPE, GIT_PULLREQUEST_MERGED);
        final JSONObject resource = gitPullRequestMergedJson.getJSONObject(RESOURCE);
        final JSONObject repository = resource.getJSONObject(REPOSITORY);
        final URI collectionUri = determineCollectionUri(repository);
        final String repoUriString = repository.getString(REMOTE_URL);
        final URI repoUri = URI.create(repoUriString);
        final String projectId = determineProjectId(repository);
        final String repoId = repository.getString(NAME);
        final String commit = determineCommit(resource);
        final String pushedBy = determineCreatedBy(resource);
        final int pullRequestId = resource.getInt("pullRequestId");

        final PullRequestMergeCommitCreatedEventArgs args = new PullRequestMergeCommitCreatedEventArgs();
        args.collectionUri = collectionUri;
        args.repoUri = repoUri;
        args.projectId = projectId;
        args.repoId = repoId;
        args.commit = commit;
        args.pushedBy = pushedBy;
        args.pullRequestId = pullRequestId;
        return args;
    }
}
