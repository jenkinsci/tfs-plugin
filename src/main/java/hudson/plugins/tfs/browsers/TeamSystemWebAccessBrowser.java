package hudson.plugins.tfs.browsers;

import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.plugins.tfs.PluginImpl;
import hudson.plugins.tfs.TeamFoundationServerScm;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;

import java.io.IOException;
import java.net.URL;

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
            return ((TeamFoundationServerScm) scm).getServerUrl();
        } else {
            throw new IllegalStateException("TFS repository browser used on a non TFS SCM");
        }
    }

    /**
     * http://server:port/UI/Pages/Scc/ViewChangeset.aspx?changeset=62643
     */
    @Override
    public URL getChangeSetLink(ChangeSet changeSet) throws IOException {
        String baseUrl = "";
        if (url != null) {
            baseUrl = DescriptorImpl.getBaseUrl(url);
        } else {
            baseUrl = String.format("%s/UI/Pages/Scc/", getServerConfiguration(changeSet)); 
        }
        return new URL(String.format("%sViewChangeset.aspx?changeset=%s", baseUrl, changeSet.getVersion()));
    }

    public DescriptorImpl getDescriptor() {
        return PluginImpl.TSWA_DESCRIPTOR;
    }

    public static final class DescriptorImpl extends Descriptor<RepositoryBrowser<?>> {

        public DescriptorImpl() {
            super(TeamSystemWebAccessBrowser.class);
        }

        @Override
        public String getDisplayName() {
            return "Team System Web Access";
        }
        
        public static String getBaseUrl(String urlExample) {
            int pos = urlExample.lastIndexOf('/');
            if (pos != -1) {
                return urlExample.substring(0, pos + 1);
            } else {
                return urlExample;
            }
        }
    }
}
