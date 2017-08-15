package hudson.plugins.tfs.listeners;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.plugins.tfs.JenkinsEventNotifier;
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
    }

    @Override
    public void onFinalized(final Run run) {
    }

    @Override
    public void onCompleted(final Run run, final @Nonnull TaskListener listener) {
        final String payload = JenkinsEventNotifier.getApiJson(run.getUrl());
        final JSONObject json = JSONObject.fromObject(payload);
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
}
