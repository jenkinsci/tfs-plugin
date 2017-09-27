package hudson.plugins.tfs.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitCommitRef;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitPullRequest;
import com.microsoft.teamfoundation.sourcecontrol.webapi.model.GitRepository;
import com.microsoft.visualstudio.services.webapi.model.IdentityRef;
import hudson.model.Action;
import hudson.plugins.git.GitStatus;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.model.servicehooks.Event;
import hudson.plugins.tfs.util.ResourceHelper;
import hudson.plugins.tfs.TeamPullRequestMergedDetailsAction;
import net.sf.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * A Git push event corresponding to the merge of a pull request on VSTS/TFS.
 */
public class GitPullRequestMergedEvent extends GitPushEvent {

    /**
     * Factory for creating a GitPullRequestMergedEvent.
     */
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
    static String determineMergeCommit(final GitPullRequest gitPullRequest) {
        final GitCommitRef lastMergeCommit = gitPullRequest.getLastMergeCommit();
        final String result = lastMergeCommit.getCommitId();
        return result;
    }

    static String determineTargetBranch(final GitPullRequest gitPullRequest) {
        // In the form of ref/heads/master
        final String targetRefName = gitPullRequest.getTargetRefName();
        String[] items = targetRefName.split("/");
        return items[items.length - 1];
    }

    @Override
    public JSONObject perform(final ObjectMapper mapper, final Event serviceHookEvent, final String message, final String detailedMessage) {
        final Object resource = serviceHookEvent.getResource();
        final GitPullRequestEx gitPullRequest = mapper.convertValue(resource, GitPullRequestEx.class);

        final PullRequestMergeCommitCreatedEventArgs args = decodeGitPullRequest(gitPullRequest, serviceHookEvent);
        final PullRequestParameterAction parameterAction = new PullRequestParameterAction(args);
        final Action teamPullRequestMergedDetailsAction = new TeamPullRequestMergedDetailsAction(gitPullRequest, message, detailedMessage, args.collectionUri.toString());
        final ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(parameterAction);
        actions.add(teamPullRequestMergedDetailsAction);
        final List<GitStatus.ResponseContributor> contributors = pollOrQueueFromEvent(args, actions, true);
        final JSONObject response = fromResponseContributors(contributors);
        return response;
    }

    static PullRequestMergeCommitCreatedEventArgs decodeGitPullRequest(final GitPullRequest gitPullRequest, final Event serviceHookEvent) {
        final GitRepository repository = gitPullRequest.getRepository();
        final URI collectionUri = determineCollectionUri(repository, serviceHookEvent);
        final String repoUriString = repository.getRemoteUrl();
        final URI repoUri = URI.create(repoUriString);
        final String projectId = determineProjectId(repository);
        final String repoId = repository.getName();
        final String commit = determineMergeCommit(gitPullRequest);
        final String pushedBy = determineCreatedBy(gitPullRequest);
        final int pullRequestId = gitPullRequest.getPullRequestId();
        final String targetBranch = determineTargetBranch(gitPullRequest);

        final PullRequestMergeCommitCreatedEventArgs args = new PullRequestMergeCommitCreatedEventArgs();
        args.collectionUri = collectionUri;
        args.repoUri = repoUri;
        args.projectId = projectId;
        args.repoId = repoId;
        args.commit = commit;
        args.pushedBy = pushedBy;
        args.pullRequestId = pullRequestId;
        args.targetBranch = targetBranch;
        return args;
    }
}
