package hudson.plugins.tfs.model;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class TeamRequestedResult extends AbstractDescribableImpl<TeamRequestedResult> {
    private final TeamResultType teamResultType;
    private Collection<String> patterns;

    @DataBoundConstructor
    public TeamRequestedResult(final TeamResultType teamResultType) {

        this.teamResultType = teamResultType;
    }

    public TeamResultType getTeamResultType() {
        return teamResultType;
    }

    public Collection<String> getPatternList() {
        return patterns == null ? Collections.EMPTY_LIST : patterns;
    }

    public String getPatterns() {
        return patterns == null ? StringUtils.EMPTY : StringUtils.join(patterns, "\n");
    }

    @DataBoundSetter
    public void setPatterns(final String patterns) {
        this.patterns = Arrays.asList(patterns.split("\n"));
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
    }
}
