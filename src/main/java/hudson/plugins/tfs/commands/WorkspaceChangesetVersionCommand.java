package hudson.plugins.tfs.commands;

/**
 * TF command for retrieving workspace latest change set version.
 *
 * @author Mario Zagar
 */
public class WorkspaceChangesetVersionCommand extends AbstractChangesetVersionCommand {
   
    private final String workspaceName;
    private final String workspaceOwner;
    
    /**
     * 
     * @param localPath the local path to get the workspace changeset version for
     * @param workspaceName name of workspace for which to get changeset version
     * @param workspaceOwner owner of TFS workspace
     */
    public WorkspaceChangesetVersionCommand(ServerConfigurationProvider provider, String localPath, String workspaceName, String workspaceOwner) {
        super(provider, localPath);

        this.workspaceName = workspaceName;
        this.workspaceOwner = workspaceOwner;
    }

    @Override
    String getVersionSpecification() {
        return "W" + workspaceName + ";" + workspaceOwner;
    }
}
