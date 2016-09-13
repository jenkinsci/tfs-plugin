package hudson.plugins.tfs;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.util.TeamStatus;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * A _Build Step_ that reports the status of an associated build as "Pending" to TFS/Team Services.
 */
public class TeamPendingStatusBuildStep extends Builder implements SimpleBuildStep {

    @DataBoundConstructor
    public TeamPendingStatusBuildStep() {

    }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener
    ) throws InterruptedException, IOException {
        if (!TeamGlobalStatusAction.isApplicable(run)){
            perform(run, listener);
        }
    }

    public void perform(final @Nonnull Run<?, ?> run, final @Nonnull TaskListener listener) {
        try {
            TeamStatus.createFromRun(run, listener, getDisplayName());
        }
        catch (final IllegalArgumentException e) {
            listener.error(e.getMessage());
        }
        catch (final Exception e) {
            e.printStackTrace(listener.error("Error while trying to update pending status in TFS/Team Services"));
        }
    }

    String getDisplayName() {
        final Descriptor<Builder> descriptor = getDescriptor();
        return descriptor.getDisplayName();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Set build pending status in TFS/Team Services";
        }
    }
}
