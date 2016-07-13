package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.ParameterizedJobMixIn;
import jenkins.triggers.SCMTriggerItem;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

/**
 * Triggers a build when we receive a VSTS post-push web hook.
 */
public class VstsPushTrigger extends Trigger<Job<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(VstsPushTrigger.class.getName());

    @DataBoundConstructor
    public VstsPushTrigger() {
    }

    public void execute() {
        // TODO: run polling
        // TODO: if polling says there are changes, create a VstsPushCause and scheduleBuild()
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(final Item item) {
            return item instanceof Job
                    && SCMTriggerItem.SCMTriggerItems.asSCMTriggerItem(item) != null
                    && item instanceof ParameterizedJobMixIn.ParameterizedJob;
        }

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to VSTS";
        }
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        if (job == null) {
            return Collections.emptyList();
        }

        return Collections.singleton(new VstsPollingAction());
    }

    public final class VstsPollingAction implements Action {

        @Override
        public String getIconFileName() {
            return "clipboard.png";
        }

        @Override
        public String getDisplayName() {
            return "VSTS hook log";
        }

        @Override
        public String getUrlName() {
            return "VstsPollLog";
        }

        // TODO: what else do we need?
    }
}
