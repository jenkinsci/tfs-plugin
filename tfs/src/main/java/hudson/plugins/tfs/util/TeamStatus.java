package hudson.plugins.tfs.util;

import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.UnsupportedIntegrationAction;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;
import hudson.plugins.tfs.model.TeamGitStatus;
import hudson.plugins.tfs.telemetry.TelemetryHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;

/**
 * Creates and adds a TeamGitStatus to the run.
 */
public final class TeamStatus {
    private TeamStatus() { }

    /**
     * Creates and adds a TeamGitStatus to the run.
     */
    public static void createFromRun(@Nonnull final Run<?, ?> run, @Nonnull final TaskListener listener, final String featureDisplayName) throws IOException {

        if (!UnsupportedIntegrationAction.isSupported(run, listener)) {
            final PrintStream logger = listener.getLogger();
            logger.print("NOTICE: ");
            logger.print("You selected '");
            logger.print(featureDisplayName);
            logger.println("' on your Jenkins job, but this option has no effect when calling the job from the 'Jenkins Queue Job' task in TFS/Team Services.");
            return;
        }

        final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
        final GitCodePushedEventArgs gitCodePushedEventArgs;
        final PullRequestMergeCommitCreatedEventArgs pullRequestMergeCommitCreatedEventArgs;

        if (commitParameter != null) {
            gitCodePushedEventArgs = commitParameter.getGitCodePushedEventArgs();
            if (commitParameter instanceof PullRequestParameterAction) {
                final PullRequestParameterAction prpa = (PullRequestParameterAction) commitParameter;
                pullRequestMergeCommitCreatedEventArgs = prpa.getPullRequestMergeCommitCreatedEventArgs();
            } else {
                pullRequestMergeCommitCreatedEventArgs = null;
            }
        } else {
            // TODO: try to guess based on what we _do_ have (i.e. RevisionParameterAction)
            return;
        }

        final URI collectionUri = gitCodePushedEventArgs.collectionUri;
        final TeamGitStatus status = TeamGitStatus.fromRun(run);

        // Send telemetry
        TelemetryHelper.sendEvent("team-status", new TelemetryHelper.PropertyMapBuilder()
                .serverContext(collectionUri.toString(), collectionUri.toString())
                .pair("feature", featureDisplayName)
                .pair("status", status.state.toString())
                .build());

        addStatus(pullRequestMergeCommitCreatedEventArgs, status);
    }

    /**
     * Create status for a (queued) Job.
     */
    public static void createFromJob(final PullRequestMergeCommitCreatedEventArgs pullRequestMergeCommitCreatedEventArgs, final Job job) throws IOException {
        final TeamGitStatus status = TeamGitStatus.fromJob(job);

        addStatus(pullRequestMergeCommitCreatedEventArgs, status);
    }

    private static void addStatus(final PullRequestMergeCommitCreatedEventArgs gitCodePushedEventArgs, final TeamGitStatus status) throws IOException {
        final PullRequestMergeCommitCreatedEventArgs pullRequestMergeCommitCreatedEventArgs;

        if (gitCodePushedEventArgs instanceof PullRequestMergeCommitCreatedEventArgs) {
            pullRequestMergeCommitCreatedEventArgs = (PullRequestMergeCommitCreatedEventArgs) gitCodePushedEventArgs;
        } else {
            pullRequestMergeCommitCreatedEventArgs = null;
        }

        final URI collectionUri = gitCodePushedEventArgs.collectionUri;
        final TeamRestClient client = new TeamRestClient(collectionUri);

        // TODO: when code is pushed and polling happens, are we sure we built against the requested commit?
        if (pullRequestMergeCommitCreatedEventArgs != null) {
            if (pullRequestMergeCommitCreatedEventArgs.iterationId == -1) {
                client.addPullRequestStatus(pullRequestMergeCommitCreatedEventArgs, status);
            } else {
                client.addPullRequestIterationStatus(pullRequestMergeCommitCreatedEventArgs, status);
            }
        }
        client.addCommitStatus(gitCodePushedEventArgs, status);

        // TODO: we could contribute an Action to the run, recording the ID of the status we created
    }
}
