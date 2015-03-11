package hudson.plugins.tfs.browsers;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.plugins.tfs.TeamFoundationServerScm;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.scm.EditType;
import hudson.scm.RepositoryBrowser;
import hudson.scm.SCM;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.FilenameUtils;
import org.kohsuke.stapler.DataBoundConstructor;

public class TeamSystemWebAccessBrowser extends TeamFoundationServerRepositoryBrowser {

    private static final long serialVersionUID = 1L;

    private final String url;
    private Boolean useRestUrls = null;
    
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
        String baseUrl;
        if (url != null) {
            baseUrl = DescriptorImpl.getBaseUrl(url);
        } else {
            baseUrl = String.format("%s/", getServerConfiguration(changeSet)); 
        }
        return baseUrl;
    }

    private boolean isUseRestUrls(ChangeSet changeSet){
        if(useRestUrls == null)
        {
            if(changeSet.getParent() == null){
                useRestUrls = false;
            }else{
                AbstractProject<?, ?> project = changeSet.getParent().build.getProject();
                SCM scm = project.getScm();
                if (scm instanceof TeamFoundationServerScm){
                    TeamFoundationServerScm tfsScm = (TeamFoundationServerScm)scm;
                    useRestUrls = tfsScm.isUseRestUrls();
                 }
            }           
        }
        return useRestUrls;
    }
    /**
     * http://tswaserver:8090/cs.aspx?cs=99
     */
    @Override
    public URL getChangeSetLink(ChangeSet changeSet) throws IOException {
        if(isUseRestUrls(changeSet))
        {
            return new URL(String.format("%s_versionControl/changeset/%s", getBaseUrlString(changeSet), changeSet.getVersion()));
        }else{
            return new URL(String.format("%scs.aspx?cs=%s", getBaseUrlString(changeSet), changeSet.getVersion()));
        }
    }

    /**
     * http://tswaserver:8090/view.aspx?path=$/Project/Folder/file.cs&cs=99
     * @param item
     * @return
     */
    public URL getFileLink(ChangeSet.Item item) throws IOException {
        if(isUseRestUrls(item.getParent()))
        {
            return new URL(String.format("%s_versionControl/changeset/%s#path=%s&_a=contents", getBaseUrlString(item.getParent()),item.getParent().getVersion(), URLEncoder.encode(item.getPath(),"UTF-8")));
        }else{
            return new URL(String.format("%sview.aspx?path=%s&cs=%s", getBaseUrlString(item.getParent()), item.getPath(), item.getParent().getVersion()));
        }
    }

    /**
     * http://tswaserver:8090/diff.aspx?opath=$/Project/Folder/file.cs&ocs=99&mpath=$/Project/Folder/file.cs&mcs=98
     * @param item
     * @return
     * @throws IOException
     */
    public URL getDiffLink(ChangeSet.Item item) throws IOException {
        ChangeSet parent = item.getParent();
        if (item.getEditType() != EditType.EDIT) {
            return null;
        }
        try {
            if(isUseRestUrls(item.getParent()))
            {
                return new URL(String.format("%s_versionControl/changeset/%s#path=%s&_a=compare", 
                    getBaseUrlString(item.getParent()),
                    item.getParent().getVersion(), 
                    URLEncoder.encode(item.getPath(),"UTF-8")));
            }else{
                return new URL(String.format("%sdiff.aspx?opath=%s&ocs=%s&mpath=%s&mcs=%s", 
                    getBaseUrlString(parent), 
                    item.getPath(),
                    getPreviousChangeSetVersion(parent), 
                    item.getPath(),
                    parent.getVersion()));
            }
        } catch (NumberFormatException nfe) {
            return null;
        }
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
        
        public static String getBaseUrl(String urlExample) throws MalformedURLException {
        	URL url = new URL(urlExample);
        	return new URL(url.getProtocol(), url.getHost(), url.getPort(), String.format("/%s", FilenameUtils.getPath(url.getPath()))).toString();
        }
    }
}
