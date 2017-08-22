//CHECKSTYLE:OFF
package hudson.plugins.tfs.actions;

import java.io.IOException;

import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

/**
 * Removes a workspace from a TFS server.
 * The tf command "workspace /delete" removes any mappings also.
 * @author Erik Ramfelt
 */
public class RemoveWorkspaceAction {

    private final String workspaceName;

    public RemoveWorkspaceAction(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    public boolean remove(Server server) throws IOException, InterruptedException {
        Workspaces workspaces = server.getWorkspaces(); 
        if (workspaces.exists(workspaceName)) {
            Workspace workspace = workspaces.getWorkspace(workspaceName);
            workspaces.deleteWorkspace(workspace);
            return true;
        }
        return false;
    }
}
