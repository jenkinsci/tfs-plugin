package hudson.plugins.tfs.model;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

public class DomainUserAccountMapper extends UserAccountMapper {
    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public DomainUserAccountMapper() {

    }

    @Override
    public String mapUserAccount(final String input) {
        return input;
    }

    @Extension
    public static final class DescriptorImpl extends UserAccountMapperDescriptor {
        @Override
        public String getDisplayName() {
            return "Resolve user using 'DOMAIN\\alias'";
        }
    }
}
