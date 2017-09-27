package hudson.plugins.tfs.listeners;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.tfs.CommitParameterAction;
import hudson.plugins.tfs.JenkinsEventNotifier;
import hudson.plugins.tfs.TeamPushCause;
import hudson.plugins.tfs.UnsupportedIntegrationAction;
import hudson.plugins.tfs.model.GitStatusState;
import java.util.List;
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
    private static final String DEFAULT_RUN_CONTEXT = "Jenkins PR build";

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
            final String context = getRunContext(run) + " started";
            JenkinsEventNotifier.sendPullRequestBuildStatusEvent(commitParameter, GitStatusState.Pending, context, targetUrl, currJob.getAbsoluteUrl());
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
        if (runResult != null && runResult.isBetterOrEqualTo(Result.SUCCESS)) {
            runGitState = GitStatusState.Succeeded;
        } else {
            runGitState = GitStatusState.Failed;
        }

        if (UnsupportedIntegrationAction.isSupported(run, listener)) {
            Job currJob = run.getParent();
            final String runDescription = run.getDescription();
            final String targetUrl = currJob.getAbsoluteUrl() + (currJob.getNextBuildNumber() - 1);
            final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
            final String context = getRunContext(run) + " completed";
            JenkinsEventNotifier.sendPullRequestBuildStatusEvent(commitParameter, runGitState, context, targetUrl, currJob.getAbsoluteUrl());
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

    private String getRunContext(final Run run) {
        List<? extends Action> actionList = run.getAllActions();
        for (final Action currAction : actionList) {
            if (currAction instanceof CauseAction) {
                for (final Cause currCause : ((CauseAction) currAction).getCauses()) {
                    if (currCause instanceof TeamPushCause) {
                        return ((TeamPushCause) currCause).getRunContext();
                    }
                }
            }
        }
        return DEFAULT_RUN_CONTEXT;
    }
}
