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
import hudson.plugins.tfs.UnsupportedIntegrationAction;
import hudson.plugins.tfs.model.GitStatusState;
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
        if (UnsupportedIntegrationAction.isSupported(run, listener)) {
            Job currJob = run.getParent();
            final String targetUrl = currJob.getAbsoluteUrl() + (currJob.getNextBuildNumber() - 1);
            final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
            JenkinsEventNotifier.sendPullRequestBuildStatusEvent(commitParameter, GitStatusState.Pending, "Jenkins CI build started", targetUrl, currJob.getAbsoluteUrl());
        }
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

        if (UnsupportedIntegrationAction.isSupported(run, listener)) {
            Job currJob = run.getParent();
            final String targetUrl = currJob.getAbsoluteUrl() + (currJob.getNextBuildNumber() - 1);
            final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
            JenkinsEventNotifier.sendPullRequestBuildStatusEvent(commitParameter, runGitState, "Jenkins CI build completed", targetUrl, currJob.getAbsoluteUrl());
        }

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

    private JSONObject createJsonFromRun(final Run run) {
        return new JSONObject();
    }
}
