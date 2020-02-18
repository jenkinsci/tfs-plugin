package hudson.plugins.tfs;

import com.microsoft.visualstudio.services.webapi.model.ResourceRef;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.telemetry.TelemetryHelper;
import hudson.plugins.tfs.util.TeamRestClient;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

/**
 * A _Post-Build Action_ that updates associated work items with a link back
 * to the Jenkins build.
 */
@SuppressWarnings("unused" /* Jenkins extension */)
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
            final String absoluteUrl = run.getAbsoluteUrl();
            final ArrayList<ResourceRef> workItems = new ArrayList<ResourceRef>();
            final URI collectionUri = TeamPullRequestMergedDetailsAction.addWorkItemsForRun(run, workItems);
            if (collectionUri != null) {
                try (final TeamRestClient client = new TeamRestClient(collectionUri)) {
                    for (final ResourceRef workItem : workItems) {
                        final String workItemIdString = workItem.getId();
                        final Integer workItemId = Integer.valueOf(workItemIdString, 10);
                        client.addHyperlinkToWorkItem(workItemId, absoluteUrl);
                    }

                    // Send telemetry
                    TelemetryHelper.sendEvent("team-workitem-update", new TelemetryHelper.PropertyMapBuilder()
                            .serverContext(collectionUri.toString(), collectionUri.toString())
                            .build());
                }
            }
        } catch (final IllegalArgumentException e) {
            listener.error(e.getMessage());
        } catch (final Exception e) {
            e.printStackTrace(listener.error("Error while trying to update associated work items in TFS/Team Services"));
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        // we don't need the outcome of any previous builds for this step
        return BuildStepMonitor.NONE;
    }

    /**
     * Class descriptor.
     */
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
