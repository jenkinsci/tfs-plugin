package hudson.plugins.tfs;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.util.VstsStatus;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * A _Build Step_ that reports the status of an associated build as "Pending" to VSTS.
 */
public class VstsPendingStatusBuildStep extends Builder implements SimpleBuildStep {

    @DataBoundConstructor
    public VstsPendingStatusBuildStep() {

    }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener
    ) throws InterruptedException, IOException {
        try {
            VstsStatus.createFromRun(run);
        }
        catch (final Exception e) {
            e.printStackTrace(listener.error("Error while trying to update pending status in TFS/Team Services"));
        }
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
