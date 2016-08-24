package hudson.plugins.tfs.util;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.model.Run;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.TeamCollectionConfiguration;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;
import hudson.plugins.tfs.model.TeamGitStatus;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;

public class TeamStatus {
    public static void createFromRun(@Nonnull final Run<?, ?> run) throws IOException {
        // TODO: also add support for a build triggered from a pull request
        final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
        final GitCodePushedEventArgs gitCodePushedEventArgs;
        final PullRequestMergeCommitCreatedEventArgs pullRequestMergeCommitCreatedEventArgs;
        if (commitParameter != null) {
            gitCodePushedEventArgs = commitParameter.getGitCodePushedEventArgs();
            if (commitParameter instanceof PullRequestParameterAction) {
                final PullRequestParameterAction prpa = (PullRequestParameterAction) commitParameter;
                pullRequestMergeCommitCreatedEventArgs = prpa.getPullRequestMergeCommitCreatedEventArgs();
            }
            else {
                pullRequestMergeCommitCreatedEventArgs = null;
            }
        }
        else {
            // TODO: try to guess based on what we _do_ have (i.e. RevisionParameterAction)
            return;
        }

        final URI collectionUri = gitCodePushedEventArgs.collectionUri;
        final TeamRestClient client = new TeamRestClient(collectionUri);

        final TeamGitStatus status = TeamGitStatus.fromRun(run);
        // TODO: when code is pushed and polling happens, are we sure we built against the requested commit?
        if (pullRequestMergeCommitCreatedEventArgs != null) {
            if (pullRequestMergeCommitCreatedEventArgs.iterationId == -1) {
                client.addPullRequestStatus(pullRequestMergeCommitCreatedEventArgs, status);
            }
            else {
                client.addPullRequestIterationStatus(pullRequestMergeCommitCreatedEventArgs, status);
            }
        }
        if (gitCodePushedEventArgs != null) {
            client.addCommitStatus(gitCodePushedEventArgs, status);
        }

        // TODO: we could contribute an Action to the run, recording the ID of the status we created
    }
}
