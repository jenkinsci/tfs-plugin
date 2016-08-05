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
        public AbstractHookEvent create() {
            return new GitPullRequestMergedEvent();
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

    static String determineCreatedBy(final JSONObject resource) {
        final JSONObject createdBy = resource.getJSONObject("createdBy");
        final String result = createdBy.getString("displayName");
        return result;
    }

    /*
    Given the following sample payload fragment:

    "lastMergeSourceCommit": {
      "commitId": "53d54ac915144006c2c9e90d2c7d3880920db49c",
      "url": "https://fabrikam.visualstudio.com/DefaultCollection/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/53d54ac915144006c2c9e90d2c7d3880920db49c"
    },
    "lastMergeTargetCommit": {
      "commitId": "a511f535b1ea495ee0c903badb68fbc83772c882",
      "url": "https://fabrikam.visualstudio.com/DefaultCollection/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/a511f535b1ea495ee0c903badb68fbc83772c882"
    },
    "lastMergeCommit": {
      "commitId": "eef717f69257a6333f221566c1c987dc94cc0d72",
      "url": "https://fabrikam.visualstudio.com/DefaultCollection/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/eef717f69257a6333f221566c1c987dc94cc0d72"
    },
    "commits": [
      {
        "commitId": "53d54ac915144006c2c9e90d2c7d3880920db49c",
        "url": "https://fabrikam.visualstudio.com/DefaultCollection/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/53d54ac915144006c2c9e90d2c7d3880920db49c"
      }
    ],

    ...we are assuming the user pushed `53d54a` (lastMergeSourceCommit) and Team Services attempted
    to merge it with `a511f5` (the tip of whatever the branch the PR is targeting, lastMergeTargetCommit),
    yielding `eef717f`.
     */
    static String determineMergeCommit(final JSONObject resource) {
        final JSONObject lastMergeCommit = resource.getJSONObject("lastMergeCommit");
        final String result = lastMergeCommit.getString("commitId");
        return result;
    }

    @Override
    public JSONObject perform(final JSONObject requestPayload) {
        final PullRequestMergeCommitCreatedEventArgs args = decodeGitPullRequestMerged(requestPayload);
        final PullRequestParameterAction parameterAction = new PullRequestParameterAction(args);
        final List<GitStatus.ResponseContributor> contributors = pollOrQueueFromEvent(args, parameterAction, true);
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
        final String commit = determineMergeCommit(resource);
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
