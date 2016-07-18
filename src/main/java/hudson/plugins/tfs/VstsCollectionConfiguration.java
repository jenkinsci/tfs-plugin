package hudson.plugins.tfs;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.plugins.tfs.util.StringHelper;
import hudson.plugins.tfs.util.UriHelper;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

public class VstsCollectionConfiguration extends AbstractDescribableImpl<VstsCollectionConfiguration> {

    private static final Charset ASCII = Charset.forName("ASCII");

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
        public FormValidation doTestCredentials(
                @QueryParameter final String collectionUrl,
                @QueryParameter final String credentialsId) {

            final String errorTemplate = "Error: %s";

            String hostName = null;
            try {
                final URL url = new URL(collectionUrl);
                hostName = url.getHost();
            }
            catch (final MalformedURLException e) {
                return FormValidation.error(errorTemplate, e.getMessage());
            }

            try {
                final StandardUsernamePasswordCredentials credential = findCredential(hostName, credentialsId);
                if (StringHelper.endsWithIgnoreCase(hostName, ".visualstudio.com")) {
                    if (credential == null) {
                        return FormValidation.error(errorTemplate, "VSTS accounts need credentials, preferably a Personal Access Token");
                    }
                }
                final int code = testConnection(collectionUrl, hostName, credential);

                if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    return FormValidation.error(errorTemplate, "HTTP code " + code);
                }
            }
            catch (final IOException e) {
                return FormValidation.error(e, errorTemplate, e.getMessage());
            }

            return FormValidation.ok("Success!");
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

            final List<StandardUsernamePasswordCredentials> matches = findCredentials(hostName);
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withAll(matches);
        }
    }

    static int testConnection(final String collectionUrl, final String hostName, final StandardUsernamePasswordCredentials credentials) throws IOException {

        final URI testUri;
        if (StringHelper.endsWithIgnoreCase(hostName, ".visualstudio.com")) {
            testUri = UriHelper.join(collectionUrl, "_apis", "connectiondata");
        }
        else {
            // for on-prem, we can hit this page since TFS 2012
            testUri = UriHelper.join(collectionUrl, "_home", "About");
        }
        final URL testUrl = testUri.toURL();
        // TODO: configure this connection so it also works through a proxy server
        final HttpURLConnection connection = (HttpURLConnection) testUrl.openConnection();
        // TODO: set user-agent
        if (credentials != null) {
            final String username = credentials.getUsername();
            final Secret secretPassword = credentials.getPassword();
            final String password = secretPassword.getPlainText();
            final String credPair = username + ":" + password;
            final byte[] credBytes = credPair.getBytes(ASCII);
            final String base64enc = DatatypeConverter.printBase64Binary(credBytes);

            connection.setRequestProperty("Authorization", "Basic" + " " + base64enc);
        }

        // TODO: extract the version from _home/About's <p class="version">Version 11.0.50727.1</p>
        final int responseCode = connection.getResponseCode();

        return responseCode;
    }

    static StandardUsernamePasswordCredentials findCredential(final String hostName, final String credentialsId) {
        final List<StandardUsernamePasswordCredentials> matches = findCredentials(hostName);
        final CredentialsMatcher matcher = CredentialsMatchers.withId(credentialsId);
        final StandardUsernamePasswordCredentials result = CredentialsMatchers.firstOrNull(matches, matcher);
        return result;
    }

    static List<StandardUsernamePasswordCredentials> findCredentials(final String hostName) {
        final Jenkins jenkins = Jenkins.getInstance();
        final HostnameRequirement requirement = new HostnameRequirement(hostName);
        final List<StandardUsernamePasswordCredentials> matches =
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        jenkins,
                        ACL.SYSTEM,
                        requirement
                );
        return matches;
    }
}
