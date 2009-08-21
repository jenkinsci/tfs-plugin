package hudson.plugins.tfs.util;

import java.io.IOException;

import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.model.Run;
import hudson.plugins.tfs.model.WorkspaceConfiguration;

/**
 * Class for retrieving the latest build configuration for a certain node.
 * As the data should be stored together with the workspace, but that is not possible
 * today so it is stored in the build itself. Latest build on a certain node always
 * contains the SCM configuration for the workspace on that node.
 * @author Erik Ramfelt, redsolo
 */
public class BuildWorkspaceConfigurationRetriever {

    public BuildWorkspaceConfiguration getLatestForNode(Node needleNode, Run<?,?> latestRun) {
        if ((latestRun == null) || !(latestRun instanceof AbstractBuild<?, ?>)) {
            return null;
        }
        
        AbstractBuild<?, ?> build = (AbstractBuild<?, ?>) latestRun;
        while ((build != null) && !build.getBuiltOn().getNodeName().equals(needleNode.getNodeName())) {
            build = build.getPreviousBuild();
        }
        
        if (build != null) {
            WorkspaceConfiguration configuration = build.getAction(WorkspaceConfiguration.class);
            if (configuration != null) {
                return new BuildWorkspaceConfiguration(configuration, build);
            }
        }

        return null;
    }
    
    public static class BuildWorkspaceConfiguration extends WorkspaceConfiguration {
        private static final long serialVersionUID = 1L;
        private final AbstractBuild<?, ?> build;
        
        public BuildWorkspaceConfiguration(WorkspaceConfiguration configuration, AbstractBuild<?, ?> build) {
            super(configuration);
            this.build = build;
        }
        public void save() throws IOException {
            if (!workspaceExists()) {
                build.getAction(WorkspaceConfiguration.class).setWorkspaceWasRemoved();
            }
            build.save();
        }        
    }
}
