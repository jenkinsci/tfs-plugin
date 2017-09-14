package hudson.plugins.tfs.listeners;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.JenkinsEventNotifier;
import hudson.plugins.tfs.PullRequestParameterAction;
import hudson.plugins.tfs.UnsupportedIntegrationAction;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.GitStatusContext;
import hudson.plugins.tfs.model.GitStatusState;
import hudson.plugins.tfs.model.PullRequestMergeCommitCreatedEventArgs;
import hudson.plugins.tfs.model.TeamGitStatus;
import hudson.plugins.tfs.util.TeamRestClient;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import net.sf.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * This class listens to the events of every Jenkins run instance.
 * Completed runs fire an event back to the JenkinsEventNotifier.
 */
@Extension
public class JenkinsRunListener extends RunListener<Run> {
    protected static final Logger log = Logger.getLogger(JenkinsRunListener.class.getName());

    public JenkinsRunListener() {
        log.fine("JenkinsRunListener: constructor");
    }

    @Override
    public void onDeleted(final Run run) {
    }

    @Override
    public void onStarted(final Run run, final TaskListener listener) {
        Job currJob = run.getParent();
        final String targetUrl = currJob.getAbsoluteUrl() + (currJob.getNextBuildNumber() - 1);
        trySetPullRequestStatus(run, listener, GitStatusState.Pending, "Jenkins CI build started", targetUrl);
    }

    @Override
    public void onFinalized(final Run run) {
    }

    @Override
    public void onCompleted(final Run run, @Nonnull final TaskListener listener) {
        log.info("onCompleted: " + run.toString());

        GitStatusState runGitState;
        final Result runResult = run.getResult();
        if (runResult.isBetterOrEqualTo(Result.SUCCESS)) {
            runGitState = GitStatusState.Succeeded;
        } else {
            runGitState = GitStatusState.Failed;
        }
        Job currJob = run.getParent();
        final String targetUrl = currJob.getAbsoluteUrl() + (currJob.getNextBuildNumber() - 1);
        trySetPullRequestStatus(run, listener, runGitState, "Jenkins CI build completed", targetUrl);

        JSONObject json = createJsonFromRun(run);
        final String payload = JenkinsEventNotifier.getApiJson(run.getUrl());
        if (payload != null) {
            json = JSONObject.fromObject(payload);
        }

        json.put("name", run.getParent().getDisplayName());
        json.put("startedBy", getStartedBy(run));

        JenkinsEventNotifier.sendJobCompletionEvent(json);
    }

    private String getStartedBy(final Run run) {
        final Cause.UserIdCause cause = (Cause.UserIdCause) run.getCause(Cause.UserIdCause.class);
        String startedBy = "";
        if (cause != null && cause.getUserId() != null) {
            startedBy = cause.getUserId();
        }
        return startedBy;
    }

    private TeamGitStatus trySetPullRequestStatus(final Run run, @Nonnull final TaskListener listener, final GitStatusState buildState, final String buildDescription, final String targetUrl) {
        try {
            final TeamGitStatus status = new TeamGitStatus();
            status.state = buildState;
            status.description = buildDescription;
            status.targetUrl = targetUrl;
            status.context = new GitStatusContext("ci-build", "jenkins-plugin");

            if (!UnsupportedIntegrationAction.isSupported(run, listener)) {
                final PrintStream logger = listener.getLogger();
                logger.print("NOTICE: ");
                logger.print("You selected '");
                logger.print(targetUrl);
                logger.println("' on your Jenkins job, but this option has no effect when calling the job from the 'Jenkins Queue Job' task in TFS/Team Services.");
                return null;
            }

            final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
            final GitCodePushedEventArgs gitCodePushedEventArgs;
            final PullRequestMergeCommitCreatedEventArgs pullRequestMergeCommitCreatedEventArgs;
            if (commitParameter != null) {
                gitCodePushedEventArgs = commitParameter.getGitCodePushedEventArgs();
                if (commitParameter instanceof PullRequestParameterAction) {
                    final PullRequestParameterAction prpa = (PullRequestParameterAction) commitParameter;
                    pullRequestMergeCommitCreatedEventArgs = prpa.getPullRequestMergeCommitCreatedEventArgs();
                    final URI collectionUri = gitCodePushedEventArgs.collectionUri;
                    final TeamRestClient client = new TeamRestClient(collectionUri);
                    return client.addPullRequestStatus(pullRequestMergeCommitCreatedEventArgs, status);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject createJsonFromRun(final Run run) {
        return new JSONObject();
    }
}
