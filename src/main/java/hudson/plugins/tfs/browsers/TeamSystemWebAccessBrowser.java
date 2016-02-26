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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class TeamSystemWebAccessBrowser extends TeamFoundationServerRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    private final String url;

    @DataBoundConstructor
    public TeamSystemWebAccessBrowser(String urlExample) {
        this.url = Util.fixEmpty(urlExample);
    }

    public String getUrl() {
        return url;
    }

    private String getServerConfiguration(ChangeSet changeset) {
        AbstractProject<?, ?> project = changeset.getParent().build.getProject();
        SCM scm = project.getScm();
        if (scm instanceof TeamFoundationServerScm) {
            return ((TeamFoundationServerScm) scm).getServerUrl(changeset.getParent().build);
        } else {
            throw new IllegalStateException("TFS repository browser used on a non TFS SCM");
        }
    }

    private String getBaseUrlString(ChangeSet changeSet) throws MalformedURLException {
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
        final QueryString qs = new QueryString();
        qs.put("id", changeSet.getVersion());
        final URL changeSetUrl = new URL(baseUrl, "_versionControl/changeset?" + qs.toString());
        return changeSetUrl;
    }

    URL createChangeSetItemLink(final ChangeSet.Item item, final String action) throws IOException {
        final ChangeSet changeSet = item.getParent();
        final URL changeSetUrl = getChangeSetLink(changeSet);
        final QueryString qs = new QueryString();
        qs.put("path", item.getPath());
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
    
    private String getPreviousChangeSetVersion(ChangeSet changeset) throws NumberFormatException {
        return Integer.toString(Integer.parseInt(changeset.getVersion()) - 1);
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        public DescriptorImpl() {
            super(TeamSystemWebAccessBrowser.class);
        }

        @Override
        public String getDisplayName() {
            return "Team System Web Access";
        }
    }
}
