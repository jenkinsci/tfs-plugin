package hudson.plugins.tfs;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.GitCodePushedEventArgs;
import hudson.plugins.tfs.model.GitStatusState;
import hudson.plugins.tfs.model.VstsGitStatus;
import hudson.plugins.tfs.util.VstsRestClient;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A _Post-Build Action_ that reports the completion status of an associated build to VSTS.
 */
public class VstsCompletedStatusPostBuildAction extends Notifier implements SimpleBuildStep {

    @DataBoundConstructor
    public VstsCompletedStatusPostBuildAction() {

    }

    @Override
    public void perform(
            @Nonnull final Run<?, ?> run,
            @Nonnull final FilePath workspace,
            @Nonnull final Launcher launcher,
            @Nonnull final TaskListener listener
    ) throws InterruptedException, IOException {
        try {
            // TODO: also add support for a build triggered from a pull request
            final CommitParameterAction commitParameter = run.getAction(CommitParameterAction.class);
            final GitCodePushedEventArgs args;
            if (commitParameter != null) {
                args = commitParameter.getGitCodePushedEventArgs();
            }
            else {
                // TODO: try to guess based on what we _do_ have (i.e. RevisionParameterAction)
                return;
            }

            final URI collectionUri = args.collectionUri;
            final StandardUsernamePasswordCredentials credentials =
                    VstsCollectionConfiguration.findCredentialsForCollection(collectionUri);
            final VstsRestClient client = new VstsRestClient(collectionUri, credentials);

            final VstsGitStatus status = VstsGitStatus.fromRun(run);
            // TODO: when code is pushed and polling happens, are we sure we built against the requested commit?
            client.addCommitStatus(args, status);

            // TODO: we could contribute an Action to the run, recording the ID of the status we created
        }
        catch (final Exception e) {
            e.printStackTrace(listener.error("Error while trying to update completion status in VSTS"));
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
            return "Set completion status for VSTS commit or pull request";
        }
    }
}
