package hudson.plugins.tfs.model;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractDescribableImpl;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeamRequestedResult extends AbstractDescribableImpl<TeamRequestedResult> {
    private final TeamResultType teamResultType;
    private String includes;

    @DataBoundConstructor
    public TeamRequestedResult(final TeamResultType teamResultType) {

        this.teamResultType = teamResultType;
    }

    public TeamResultType getTeamResultType() {
        return teamResultType;
    }

    public String getIncludes() {
        return includes;
    }

    @DataBoundSetter
    public void setIncludes(final String includes) {
        this.includes = includes;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TeamRequestedResult> {

        @Override
        public String getDisplayName() {
            return "Requested build result";
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillTeamResultTypeItems() {
            final TeamResultType[] values = TeamResultType.values();
            final ListBoxModel result = new ListBoxModel(values.length);

            for (final TeamResultType value : values) {
                result.add(value.getDisplayName(), value.name());
            }
            return result;
        }

        public FormValidation doCheckIncludes(
                @AncestorInPath final AbstractProject project,
                @QueryParameter final String value) throws IOException {
            if (project == null) {
                return FormValidation.ok();
            }
            return FilePath.validateFileMask(project.getSomeWorkspace(), value);
        }
    }
}
