package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import javax.annotation.Nonnull;

/**
 * Posts the status to TFS/Team Services if the {@link TeamGlobalStatusAction} was contributed.
 */
@Extension
public class TeamGlobalStatusPoster extends RunListener<AbstractBuild> {

    @Override
    public void onStarted(final AbstractBuild build, final TaskListener listener) {
        if (TeamGlobalStatusAction.isApplicable(build)) {
            final TeamPendingStatusBuildStep step = new TeamPendingStatusBuildStep();
            step.perform(build, listener);
        }
    }

    @Override
    public void onCompleted(final AbstractBuild build, @Nonnull final TaskListener listener) {
        if (TeamGlobalStatusAction.isApplicable(build)) {
            final TeamCompletedStatusPostBuildAction step = new TeamCompletedStatusPostBuildAction();
            step.perform(build, listener);
        }
    }

}
