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
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * A _Post-Build Action_ that reports the completion status of an associated build to TFS/Team Services.
 */
public class TeamCompletedStatusPostBuildAction extends Notifier implements SimpleBuildStep {

    @DataBoundConstructor
    public TeamCompletedStatusPostBuildAction() {

    }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener
    ) throws InterruptedException, IOException {
        try {
            TeamStatus.createFromRun(run, listener, getDisplayName());
        }
        catch (final IllegalArgumentException e) {
            listener.error(e.getMessage());
        }
        catch (final Exception e) {
            e.printStackTrace(listener.error("Error while trying to update completion status in TFS/Team Services"));
        }
    }

    String getDisplayName() {
        final Descriptor<Builder> descriptor = getDescriptor();
        return descriptor.getDisplayName();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        // we don't need the outcome of any previous builds for this step
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Set build completion status in TFS/Team Services";
        }
    }
}
