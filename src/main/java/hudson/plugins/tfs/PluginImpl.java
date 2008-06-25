package hudson.plugins.tfs;

import hudson.Plugin;
import hudson.scm.SCMS;

/**
 * Team Foundation Server plugin.
 * 
 * @author Erik Ramfelt
 */
public class PluginImpl extends Plugin {

    public static final TeamFoundationServerScm.DescriptorImpl TFS_DESCRIPTOR = new TeamFoundationServerScm.DescriptorImpl();

    /**
     * Registers SCMDescriptors with Hudson.
     */
    @Override
    public void start() throws Exception {
        SCMS.SCMS.add(TFS_DESCRIPTOR);
        super.start();
    }
}