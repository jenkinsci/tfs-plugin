package hudson.plugins.tfs;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * A _Post-Build Action_ that updates associated work items with a link back
 * to the Jenkins build.
 */
public class TeamUpdateWorkItemPostBuildAction extends Notifier implements SimpleBuildStep {

    @DataBoundConstructor
    public TeamUpdateWorkItemPostBuildAction() {

    }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener
    ) throws InterruptedException, IOException {
        try {
            // TODO: implement
        }
        catch (final IllegalArgumentException e) {
            listener.error(e.getMessage());
        }
        catch (final Exception e) {
            e.printStackTrace(listener.error("Error while trying to update associated work items in TFS/Team Services"));
        }
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
            return "Add link to associated work items in TFS/Team Services";
        }
    }
}
