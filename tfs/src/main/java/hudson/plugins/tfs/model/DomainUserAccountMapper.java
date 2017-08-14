package hudson.plugins.tfs.model;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Mapper for Domain User accounts.
 */
public class DomainUserAccountMapper extends UserAccountMapper {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for data binding.
     */
    @DataBoundConstructor
    public DomainUserAccountMapper() {

    }

    @Override
    public String mapUserAccount(final String input) {
        return input;
    }

    /**
     * Class descriptor.
     */
    @Extension
    public static final class DescriptorImpl extends UserAccountMapperDescriptor {
        @Override
        public String getDisplayName() {
            return "Resolve user using 'DOMAIN\\alias'";
        }
    }
}
