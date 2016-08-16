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

    private final boolean configFolderPerNode;
    private final String computerName;

    @SuppressWarnings("unused" /* Needed by Serializable interface */)
    private ExtraSettings() {
        this(false, null);
    }

    public ExtraSettings(final boolean configFolderPerNode, final String computerName) {
        this.configFolderPerNode = configFolderPerNode;
        this.computerName = computerName;
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

    public String getComputerName() {
        return computerName;
    }
}
