//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import jenkins.model.Jenkins;

import java.io.Serializable;

public abstract class CredentialsConfigurer implements ExtensionPoint, Describable<CredentialsConfigurer>, Serializable {
    private static final long serialVersionUID = 1L;

    public final String getDisplayName() {
        return getDescriptor().getDisplayName();
    }

    public CredentialsConfigurerDescriptor getDescriptor() {
        final Jenkins jenkins = Jenkins.getActiveInstance();
        return (CredentialsConfigurerDescriptor) jenkins.getDescriptorOrDie(getClass());
    }

    public abstract StandardUsernamePasswordCredentials getCredentials(final String collectionUri);

    public static DescriptorExtensionList<CredentialsConfigurer, CredentialsConfigurerDescriptor> all() {
        final Jenkins jenkins = Jenkins.getActiveInstance();
        return jenkins.getDescriptorList(CredentialsConfigurer.class);
    }
}
