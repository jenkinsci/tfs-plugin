package hudson.plugins.tfs;

import hudson.Plugin;
import hudson.plugins.tfs.browsers.TeamSystemWebAccessBrowser;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCMS;

/**
 * Team Foundation Server plugin.
 * 
 * @author Erik Ramfelt
 */
public class PluginImpl extends Plugin {

    public static final TeamFoundationServerScm.DescriptorImpl TFS_DESCRIPTOR = new TeamFoundationServerScm.DescriptorImpl();
    
    public static final TeamSystemWebAccessBrowser.DescriptorImpl TSWA_DESCRIPTOR = new TeamSystemWebAccessBrowser.DescriptorImpl();

    /**
     * Registers SCMDescriptors with Hudson.
     */
    @Override
    public void start() throws Exception {
        SCMS.SCMS.add(TFS_DESCRIPTOR);
        RepositoryBrowsers.LIST.add(TSWA_DESCRIPTOR);
        super.start();
    }
}