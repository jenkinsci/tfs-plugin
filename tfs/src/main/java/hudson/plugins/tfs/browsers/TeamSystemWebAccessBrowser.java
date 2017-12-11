package hudson.plugins.tfs.browsers;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.plugins.tfs.TeamFoundationServerScm;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.QueryString;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *  Provides information and links back to the TFS/VSTS repository for the current change set.
 */
public class TeamSystemWebAccessBrowser extends TeamFoundationServerRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    private final String url;

    @DataBoundConstructor
    public TeamSystemWebAccessBrowser(final String urlExample) {
        this.url = Util.fixEmpty(urlExample);
    }

    public String getUrl() {
        return url;
    }

    private String getServerConfiguration(final ChangeSet changeset) {
        AbstractProject<?, ?> project = changeset.getParent().build.getProject();
        SCM scm = project.getScm();
        if (scm instanceof TeamFoundationServerScm) {
            return ((TeamFoundationServerScm) scm).getServerUrl(changeset.getParent().build);
        } else {
            final DescriptorImpl descriptor = (DescriptorImpl) getDescriptor();
            final String displayName = descriptor.getDisplayName();
            throw new IllegalStateException("'" + displayName + "' repository browser can only be used with the 'Team Foundation Server' SCM");
        }
    }

    private String getBaseUrlString(final ChangeSet changeSet) throws MalformedURLException {
        String baseUrl = url;
        if (baseUrl == null) {
            baseUrl = getServerConfiguration(changeSet);
        }
        baseUrl = normalizeToEndWithSlash(new URL(baseUrl)).toString();
        return baseUrl;
    }

    /**
     * Gets the link to a specific change set.
     */
    @Override
    public URL getChangeSetLink(final ChangeSet changeSet) throws IOException {
        final String baseUrlString = getBaseUrlString(changeSet);
        final URL baseUrl = new URL(baseUrlString);
        return new URL(baseUrl, "_versionControl/changeset/" + changeSet.getVersion());
    }

    private URL createChangeSetItemLink(final ChangeSet.Item item, final String action) throws IOException {
        final ChangeSet changeSet = item.getParent();
        final URL changeSetUrl = getChangeSetLink(changeSet);
        final QueryString qs = new QueryString();
        qs.put("path", item.getPath());
        qs.put("version", changeSet.getVersion());
        qs.put("_a", action);
        return new URL(changeSetUrl, "#" + qs.toString());
    }

    /**
     * Gets the link for a specific file in a change set.
     */
    public URL getFileLink(final ChangeSet.Item item) throws IOException {
        return createChangeSetItemLink(item, "contents");
    }

    /**
     * Gets the link to compare a specific file in a change set.
     */
    public URL getDiffLink(final ChangeSet.Item item) throws IOException {
        if (item.getEditType() != EditType.EDIT) {
            return null;
        }
        return createChangeSetItemLink(item, "compare");
    }

    /**
     * Gets the descriptor for the repository.
     */
    @Extension
    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        public DescriptorImpl() {
            super(TeamSystemWebAccessBrowser.class);
        }

        @Override
        public String getDisplayName() {
            return "Microsoft Team Foundation Server/Visual Studio Team Services";
        }
    }
}
