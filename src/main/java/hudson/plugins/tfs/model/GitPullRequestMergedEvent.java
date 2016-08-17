package hudson.plugins.tfs.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitCommitRef;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPullRequest;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import com.microsoft.visualstudio.services.webapi.model.IdentityRef;
import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.util.ResourceHelper;
import net.sf.json.JSONObject;

import java.io.IOException;
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
            return ResourceHelper.fetchAsString(this.getClass(), "GitPullRequestMergedEvent.json");
        }
    }

    static String determineCreatedBy(final JSONObject resource) {
        final JSONObject createdBy = resource.getJSONObject("createdBy");
        final String result = createdBy.getString("displayName");
        return result;
    }

    static String determineCreatedBy(final GitPullRequest gitPullRequest) {
        final IdentityRef createdBy = gitPullRequest.getCreatedBy();
        final String result = createdBy.getDisplayName();
        return result;
    }

    /*
    Given the following sample payload fragment:

    "lastMergeSourceCommit": {
      "commitId": "53d54ac915144006c2c9e90d2c7d3880920db49c",
      "url": "https://fabrikam.visualstudio.com/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/53d54ac915144006c2c9e90d2c7d3880920db49c"
    },
    "lastMergeTargetCommit": {
      "commitId": "a511f535b1ea495ee0c903badb68fbc83772c882",
      "url": "https://fabrikam.visualstudio.com/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/a511f535b1ea495ee0c903badb68fbc83772c882"
    },
    "lastMergeCommit": {
      "commitId": "eef717f69257a6333f221566c1c987dc94cc0d72",
      "url": "https://fabrikam.visualstudio.com/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/eef717f69257a6333f221566c1c987dc94cc0d72"
    },
    "commits": [
      {
        "commitId": "53d54ac915144006c2c9e90d2c7d3880920db49c",
        "url": "https://fabrikam.visualstudio.com/_apis/git/repositories/4bc14d40-c903-45e2-872e-0462c7748079/commits/53d54ac915144006c2c9e90d2c7d3880920db49c"
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

    static String determineMergeCommit(final GitPullRequest gitPullRequest) {
        final GitCommitRef lastMergeCommit = gitPullRequest.getLastMergeCommit();
        final String result = lastMergeCommit.getCommitId();
        return result;
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final JsonParser resourceParser) {
        final GitPullRequest gitPullRequest;
        try {
            gitPullRequest = mapper.readValue(resourceParser, GitPullRequest.class);
        }
        catch (final IOException e) {
            throw new Error(e);
        }

        final PullRequestMergeCommitCreatedEventArgs args = decodeGitPullRequest(gitPullRequest);
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

    static PullRequestMergeCommitCreatedEventArgs decodeGitPullRequest(final GitPullRequest gitPullRequest) {
        final GitRepository repository = gitPullRequest.getRepository();
        final URI collectionUri = determineCollectionUri(repository);
        final String repoUriString = repository.getRemoteUrl();
        final URI repoUri = URI.create(repoUriString);
        final String projectId = determineProjectId(repository);
        final String repoId = repository.getName();
        final String commit = determineMergeCommit(gitPullRequest);
        final String pushedBy = determineCreatedBy(gitPullRequest);
        final int pullRequestId = gitPullRequest.getPullRequestId();

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
