package hudson.plugins.tfs;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Triggers a build when we receive a TFS/Team Services Git code push event in a TFS pull request.
 */
public class TeamPRPushTrigger extends TeamPushTrigger {

    @DataBoundConstructor
    public TeamPRPushTrigger() {
    }

    /**
     * This class extends trigger descriptor class from TeamPushTrigger, creating a separate check box for TeamPRPushTrigger.
     */
    @Extension
    public static class DescriptorImpl extends TeamPushTrigger.DescriptorImpl {

        @Override
        public String getDisplayName() {
            return "Build when a change is pushed to a TFS pull request";
        }
    }
}
