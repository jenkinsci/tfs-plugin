package hudson.plugins.tfs;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Posts the status to TFS/Team Services if the {@link TeamGlobalStatusAction} was contributed.
 */
@Extension
public class TeamGlobalStatusPoster extends RunListener<AbstractBuild> {

    @Override
    public void onStarted(final AbstractBuild build, final TaskListener listener) {
        if (TeamGlobalStatusAction.isApplicable(build)) {
            final TeamPendingStatusBuildStep step = new TeamPendingStatusBuildStep();
            performStep(step, build, listener);
        }
    }

    @Override
    public void onCompleted(final AbstractBuild build, @Nonnull final TaskListener listener) {
        if (TeamGlobalStatusAction.isApplicable(build)) {
            final TeamCompletedStatusPostBuildAction step = new TeamCompletedStatusPostBuildAction();
            performStep(step, build, listener);
        }
    }

    static void performStep(final SimpleBuildStep step, final AbstractBuild build, final TaskListener listener) {
        final Jenkins jenkins = Jenkins.getInstance();
        final FilePath workspace = build.getWorkspace();
        final Launcher launcher = jenkins.createLauncher(listener);
        try {
            step.perform(build, workspace, launcher, listener);
        }
        catch (final InterruptedException e) {
            throw new Error(e);
        }
        catch (final IOException e) {
            throw new Error(e);
        }
    }
}
