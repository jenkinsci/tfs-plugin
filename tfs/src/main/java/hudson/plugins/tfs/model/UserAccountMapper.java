package hudson.plugins.tfs.model;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import jenkins.model.Jenkins;

import java.io.Serializable;

/**
 * Extends Jenkins to add our own UserAccountMapper.
 */
public abstract class UserAccountMapper implements ExtensionPoint, Describable<UserAccountMapper>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Returns the display name for this class.
     */
    public final String getDisplayName() {
        return getDescriptor().getDisplayName();
    }

    /**
     * Returns the class descriptor.
     */
    public UserAccountMapperDescriptor getDescriptor() {
        final Jenkins jenkins = Jenkins.getActiveInstance();
        return (UserAccountMapperDescriptor) jenkins.getDescriptorOrDie(getClass());
    }

    /**
     * Abstract method to map an account from user input.
     */
    public abstract String mapUserAccount(final String input);

    /**
     * Gets all user account descriptors.
     */
    public static DescriptorExtensionList<UserAccountMapper, UserAccountMapperDescriptor> all() {
        final Jenkins jenkins = Jenkins.getActiveInstance();
        return jenkins.getDescriptorList(UserAccountMapper.class);
    }
}
