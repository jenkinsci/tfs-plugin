package hudson.plugins.tfs;

import hudson.Extension;
import hudson.model.Job;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Triggers a build when we receive a TFS/Team Services Git code push event in a TFS pull request.
 */
public class TeamPRPushTrigger extends TeamPushTrigger {

    private String targetBranches;

    @DataBoundConstructor
    public TeamPRPushTrigger() {
    }

    public TeamPRPushTrigger(final Job<?, ?> job, final String targetBranches, final String jobContext) {
        this.job = job;
        this.targetBranches = targetBranches;

        setJobContext(jobContext);
    }

    public String getTargetBranches() {
        return targetBranches;
    }

    @DataBoundSetter
    public void setTargetBranches(final String targetBranches) {
        this.targetBranches = targetBranches;
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
