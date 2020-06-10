//CHECKSTYLE:OFF
package hudson.plugins.tfs;

import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.DomainSpecification;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import com.cloudbees.plugins.credentials.domains.HostnameSpecification;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.microsoft.tfs.core.exceptions.TFSUnauthorizedException;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.plugins.tfs.model.ConnectionParameters;
import hudson.plugins.tfs.model.ListOfGitRepositories;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.util.StringHelper;
import hudson.plugins.tfs.util.TeamRestClient;
import hudson.plugins.tfs.util.UriHelper;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TeamCollectionConfiguration extends AbstractDescribableImpl<TeamCollectionConfiguration> {

    private static final Logger LOGGER = Logger.getLogger(TeamCollectionConfiguration.class.getName());

    private final String collectionUrl;
    private final String credentialsId;
    private ConnectionParameters connectionParameters;

    @DataBoundConstructor
    public TeamCollectionConfiguration(final String collectionUrl, final String credentialsId) {
        this.collectionUrl = collectionUrl;
        this.credentialsId = credentialsId;
    }

    public String getCollectionUrl() {
        return collectionUrl;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public ConnectionParameters getConnectionParameters() {
        if (connectionParameters == null) {
            connectionParameters = new ConnectionParameters();
        }
        return connectionParameters;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TeamCollectionConfiguration> {

        @Override
        public String getDisplayName() {
            return "Team Project Collection";
        }

        @SuppressWarnings("unused")
        public FormValidation doCheckCollectionUrl(
                @QueryParameter final String value) {

            if (StringUtils.isBlank(value)) {
                return FormValidation.warning("Please provide a value");
            }

            final URI uri;
            try {
                uri = new URI(value);
            }
            catch (final URISyntaxException e) {
                return FormValidation.error("Malformed TFS/Team Services collection URL (%s)", e.getMessage());
            }

            final String hostName = uri.getHost();
            if (StringUtils.isBlank(hostName)) {
                return FormValidation.error("Please provide a host name");
            }
            if (isTeamServices(hostName)) {
                return checkTeamServices(uri);
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
                final URI uri = new URI(collectionUrl);
                hostName = uri.getHost();
            }
            catch (final URISyntaxException e) {
                return FormValidation.error(errorTemplate, e.getMessage());
            }

            try {
                final StandardUsernamePasswordCredentials credential = findCredentialsById(credentialsId);
                if (isTeamServices(hostName)) {
                    if (credential == null) {
                        return FormValidation.error(errorTemplate, "Team Services accounts need credentials, preferably a Personal Access Token");
                    }
                }
                return testConnection(collectionUrl, credential);
            }
            catch (final IOException e) {
                return FormValidation.error(e, errorTemplate, e.getMessage());
            }
        }

        @SuppressWarnings("unused")
        public ListBoxModel doFillCredentialsIdItems(
            @QueryParameter final String collectionUrl) {

            final Jenkins jenkins = Jenkins.getInstance();

            String hostName = null;
            try {
                final URI uri = new URI(collectionUrl);
                hostName = uri.getHost();
            }
            catch (final URISyntaxException ignored) {
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

    static FormValidation checkTeamServices(final URI uri) {
        if (UriHelper.hasPath(uri)) {
            return FormValidation.error("A Team Services collection URL must have an empty path.");
        }
        return FormValidation.ok();
    }

    static boolean areSameCollectionUri(final URI a, final URI b) {
        if (a == null) {
            throw new IllegalArgumentException("Parameter 'a' is null");
        }
        if (b == null) {
            throw new IllegalArgumentException("Parameter 'b' is null");
        }

        final String aHost = a.getHost();
        final String bHost = b.getHost();
        if (isTeamServices(aHost) && isTeamServices(bHost)) {
            return StringHelper.equalIgnoringCase(aHost, bHost);
        }

        return UriHelper.areSame(a, b);
    }

    public static boolean isTeamServices(final String hostName) {
        return StringHelper.endsWithIgnoreCase(hostName, ".visualstudio.com");
    }

    static FormValidation testConnection(final String collectionUri, final StandardUsernamePasswordCredentials credentials) throws IOException {

        final Server server = Server.create(null, null, collectionUri, credentials, null, null);
        try {
            final MockableVersionControlClient vcc = server.getVersionControlClient();
            return FormValidation.ok("Success via SOAP API.");
        }
        catch (final TFSUnauthorizedException e) {
            // performing TFVC requires All Scopes and someone might be setting up for Git only; ignore
        }
        final TeamRestClient client = new TeamRestClient(collectionUri, credentials);

        try {
            final ListOfGitRepositories repositories = client.getRepositories();
            if (repositories.count < 1) {
                return FormValidation.warning("There does not seem to be any Git repositories");
            }
            return FormValidation.ok("Success via REST API.");
        }
        catch (final IOException e) {
            return FormValidation.error("Error: " + e.getMessage());
        }
    }

    static StandardUsernamePasswordCredentials findCredential(final String hostName, final String credentialsId) {
        final List<StandardUsernamePasswordCredentials> matches = findCredentials(hostName);
        final CredentialsMatcher matcher = CredentialsMatchers.withId(credentialsId);
        final StandardUsernamePasswordCredentials result = CredentialsMatchers.firstOrNull(matches, matcher);
        return result;
    }

    public static List<StandardUsernamePasswordCredentials> findCredentials(final String hostName) {
        final Jenkins jenkins = Jenkins.getInstance();
        return findCredentials(hostName, jenkins);
    }

    public static List<StandardUsernamePasswordCredentials> findCredentials(final String hostName, ItemGroup own) {
        final HostnameRequirement requirement = new HostnameRequirement(hostName);
        final List<StandardUsernamePasswordCredentials> matches =
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        own,
                        ACL.SYSTEM,
                        requirement
                );
        return matches;
    }

    public static List<StandardUsernamePasswordCredentials> findCredentials(final String hostName, Item own) {
        final HostnameRequirement requirement = new HostnameRequirement(hostName);
        final List<StandardUsernamePasswordCredentials> matches =
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        own,
                        ACL.SYSTEM,
                        requirement
                );
        return matches;
    }

    public static StandardUsernamePasswordCredentials findCredentialsById(final String credentialsId) {
        final Jenkins jenkins = Jenkins.getInstance();
        final List<StandardUsernamePasswordCredentials> matches =
                CredentialsProvider.lookupCredentials(
                        StandardUsernamePasswordCredentials.class,
                        jenkins,
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()
                );
        final CredentialsMatcher matcher = CredentialsMatchers.withId(credentialsId);
        final StandardUsernamePasswordCredentials result = CredentialsMatchers.firstOrNull(matches, matcher);
        return result;
    }

    public static String setCredentials(final String hostName, String username, String password) {
        List<DomainSpecification> domainSpecifications = new ArrayList<>();
        domainSpecifications.add(new HostnameSpecification(hostName, null));
        Domain domain = new Domain("Generated for " + hostName, "", domainSpecifications);

        SystemCredentialsProvider.getInstance().getDomainCredentialsMap().put(domain, new ArrayList<Credentials>());

        String credentialsId;
        StandardUsernamePasswordCredentials newCredential = new UsernamePasswordCredentialsImpl(
                CredentialsScope.GLOBAL,
                credentialsId = UUID.randomUUID().toString(),
                "Generated for " + username,
                username,
                password
        );
        SystemCredentialsProvider.getInstance().getDomainCredentialsMap().get(domain).add(newCredential);

        try {
            SystemCredentialsProvider.getInstance().save();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "SystemCredentialsProvider instance save failed: ", ex);
        }

        return credentialsId;
    }

    // TODO: we'll probably also want findCredentialsForGitRepo, where we match part of the URL path
    public static StandardUsernamePasswordCredentials findCredentialsForCollection(final URI collectionUri) {
        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        // TODO: consider using a different data structure to speed up this look-up
        final List<TeamCollectionConfiguration> pairs = config.getCollectionConfigurations();
        for (final TeamCollectionConfiguration pair : pairs) {
            final String candidateCollectionUrlString = pair.getCollectionUrl();
            final URI candidateCollectionUri = URI.create(candidateCollectionUrlString);
            if (areSameCollectionUri(candidateCollectionUri, collectionUri)) {
                final String credentialsId = pair.credentialsId;
                if (credentialsId != null) {
                    return findCredentialsById(credentialsId);
                }
                return null;
            }
        }
        final String template = "There is no team project collection configured for the URL '%1$s'.%n" +
                "Please go to Jenkins > Manage Jenkins > Configure System and then " +
                "add a Team Project Collection with a Collection URL of '%1$s'.";
        final String message = String.format(template, collectionUri);
        throw new IllegalArgumentException(message);
    }

    public static TeamCollectionConfiguration findCollection(final URI collectionUri) {
        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        // TODO: consider using a different data structure to speed up this look-up
        final List<TeamCollectionConfiguration> pairs = config.getCollectionConfigurations();
        for (final TeamCollectionConfiguration pair : pairs) {
            final String candidateCollectionUrlString = pair.getCollectionUrl();
            final URI candidateCollectionUri = URI.create(candidateCollectionUrlString);
            if (areSameCollectionUri(candidateCollectionUri, collectionUri)) {
                return pair;
            }
        }
        return null;
    }

    public static List<TeamCollectionConfiguration> getConnectedCollections() {
        final List<TeamCollectionConfiguration> connectedCollections = new ArrayList<>();
        final TeamPluginGlobalConfig config = TeamPluginGlobalConfig.get();
        final List<TeamCollectionConfiguration> collections = config.getCollectionConfigurations();
        for (final TeamCollectionConfiguration c : collections) {
            if (c.getConnectionParameters().isSendJobCompletionEvents() && StringUtils.isNotEmpty(c.getConnectionParameters().getTeamCollectionUrl())) {
                connectedCollections.add(c);
            }
        }
        return connectedCollections;
    }
}
