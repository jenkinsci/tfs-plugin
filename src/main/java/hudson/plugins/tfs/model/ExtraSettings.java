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
    private String computerName;

    public static final ExtraSettings DEFAULT = new ExtraSettings();

    @SuppressWarnings("unused" /* Needed by Serializable interface */)
    private ExtraSettings() {
    }

    public ExtraSettings(final TeamPluginGlobalConfig teamPluginGlobalConfig) {
        if (teamPluginGlobalConfig != null) {
            this.configFolderPerNode = teamPluginGlobalConfig.isConfigFolderPerNode();
            this.computerName = Computer.currentComputer().getName();
        }
        else {
            this.configFolderPerNode = false;
            this.computerName = null;
        }
    }

    public boolean isConfigFolderPerNode() {
        return configFolderPerNode;
    }

    public void setConfigFolderPerNode(final boolean configFolderPerNode) {
        this.configFolderPerNode = configFolderPerNode;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(final String computerName) {
        this.computerName = computerName;
    }
}
