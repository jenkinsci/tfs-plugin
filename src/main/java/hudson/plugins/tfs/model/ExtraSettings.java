package hudson.plugins.tfs.model;

import hudson.model.Computer;
import hudson.plugins.tfs.TeamPluginGlobalConfig;

import java.io.Serializable;

/**
 * This class exists to shuttle settings between MASTER to remote nodes,
 * who would otherwise be unable to determine said settings because they
 * don't have access to the {@link jenkins.model.Jenkins} instance or
 * anything that could be obtained from it.
 */
public class ExtraSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean configFolderPerNode;
    private String nodeComputerName;

    public static final ExtraSettings DEFAULT = new ExtraSettings();

    @SuppressWarnings("unused" /* Needed by Serializable interface */)
    private ExtraSettings() {
    }

    public ExtraSettings(final TeamPluginGlobalConfig teamPluginGlobalConfig) {
        if (teamPluginGlobalConfig != null) {
            this.configFolderPerNode = teamPluginGlobalConfig.isConfigFolderPerNode();
            final Computer currentComputer = Computer.currentComputer();
            if (currentComputer != null) {
                this.nodeComputerName = currentComputer.getName();
            }
            else {
                this.nodeComputerName = "";
            }
        }
        else {
            this.configFolderPerNode = false;
            this.nodeComputerName = null;
        }
    }

    public boolean isConfigFolderPerNode() {
        return configFolderPerNode;
    }

    public void setConfigFolderPerNode(final boolean configFolderPerNode) {
        this.configFolderPerNode = configFolderPerNode;
    }

    public String getNodeComputerName() {
        return nodeComputerName;
    }

    public void setNodeComputerName(final String nodeComputerName) {
        this.nodeComputerName = nodeComputerName;
    }
}
