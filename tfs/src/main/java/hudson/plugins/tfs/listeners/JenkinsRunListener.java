package hudson.plugins.tfs.listeners;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import hudson.plugins.tfs.TeamCompletedStatusPostBuildAction;
import hudson.plugins.tfs.TeamPendingStatusBuildStep;
import hudson.plugins.tfs.UnsupportedIntegrationAction;

import javax.annotation.Nonnull;
import java.util.logging.Logger;


/**
 * This class listens to the events of every Jenkins run instance.
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
            final TeamPendingStatusBuildStep step = new TeamPendingStatusBuildStep();
            step.perform(run, listener);
        }
    }

    @Override
    public void onFinalized(final Run run) {
    }

    @Override
    public void onCompleted(final Run run, @Nonnull final TaskListener listener) {
        log.info("onCompleted: " + run.toString());
        if (UnsupportedIntegrationAction.isSupported(run, listener)) {
            final TeamCompletedStatusPostBuildAction step = new TeamCompletedStatusPostBuildAction();
            step.perform(run, listener);
        }
    }
}
