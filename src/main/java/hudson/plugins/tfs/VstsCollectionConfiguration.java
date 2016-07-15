package hudson.plugins.tfs;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class VstsCollectionConfiguration extends AbstractDescribableImpl<VstsCollectionConfiguration> {

    private final String collectionUrl;
    private final String credentialsId;

    @DataBoundConstructor
    public VstsCollectionConfiguration(final String collectionUrl, final String credentialsId) {
        this.collectionUrl = collectionUrl;
        this.credentialsId = credentialsId;
    }

    public String getCollectionUrl() {
        return collectionUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<VstsCollectionConfiguration> {

        @Override
        public String getDisplayName() {
            return "Team Project Collection";
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckCollectionUrl(
                @QueryParameter final String value) {

            try {
                new URL(value);
            }
            catch (MalformedURLException e) {
                return FormValidation.error("Malformed VSTS/TFS collection URL (%s)", e.getMessage());
            }

            // TODO: check that it's not a deep URL to a repository, work item, API endpoint, etc.

            return FormValidation.ok();
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillCredentialsIdItems(
            @QueryParameter final String collectionUrl) {

            final Jenkins jenkins = Jenkins.getInstance();

            String hostName = null;
            try {
                final URL url = new URL(collectionUrl);
                hostName = url.getHost();
            }
            catch (final MalformedURLException ignored) {
            }

            if (hostName == null || !jenkins.hasPermission(Jenkins.ADMINISTER)) {
                return new ListBoxModel();
            }

            final HostnameRequirement requirement = new HostnameRequirement(hostName);
            final List<StandardUsernamePasswordCredentials> matches =
                CredentialsProvider.lookupCredentials(
                    StandardUsernamePasswordCredentials.class,
                    jenkins,
                    ACL.SYSTEM,
                    requirement
                );
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withAll(matches);
        }
    }
}
