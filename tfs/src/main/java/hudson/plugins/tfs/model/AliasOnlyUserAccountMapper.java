package hudson.plugins.tfs.model;

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Maps user accounts without using the DOMAIN.
 */
public class AliasOnlyUserAccountMapper extends UserAccountMapper {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    @DataBoundConstructor
    public AliasOnlyUserAccountMapper() {

    }

    @Override
    public String mapUserAccount(final String input) {
        final String[] split = input.split("\\\\");
        final String result;
        if (split.length == 2) {
            result = split[1];
        } else {
            result = input;
        }
        return result;
    }

    /**
     * A descriptor for the class.
     */
    @Extension
    public static final class DescriptorImpl extends UserAccountMapperDescriptor {
        @Override
        public String getDisplayName() {
            return "Resolve user using 'alias' only, removing 'DOMAIN\\'";
        }
    }
}
