package hudson.plugins.tfs.model;

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import jenkins.model.Jenkins;

import java.io.Serializable;

public abstract class UserAccountMapper implements ExtensionPoint, Describable<UserAccountMapper>, Serializable {
    private static final long serialVersionUID = 1L;

    public final String getDisplayName() {
        return getDescriptor().getDisplayName();
    }

    public UserAccountMapperDescriptor getDescriptor() {
        final Jenkins jenkins = Jenkins.getInstance();
        return (UserAccountMapperDescriptor) jenkins.getDescriptorOrDie(getClass());
    }

    public abstract String mapUserAccount(final String input);

    public static DescriptorExtensionList<UserAccountMapper, UserAccountMapperDescriptor> all() {
        final Jenkins jenkins = Jenkins.getInstance();
        return jenkins.getDescriptorList(UserAccountMapper.class);
    }
}
