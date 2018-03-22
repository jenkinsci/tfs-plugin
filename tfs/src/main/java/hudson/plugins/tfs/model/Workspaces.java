//CHECKSTYLE:OFF
package hudson.plugins.tfs.model;

import hudson.plugins.tfs.commands.DeleteWorkspaceCommand;
import hudson.plugins.tfs.commands.GetWorkspaceMappingCommand;
import hudson.plugins.tfs.commands.ListWorkspacesCommand;
import hudson.plugins.tfs.commands.NewWorkspaceCommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that creates, deletes and gets workspaces from a TeamFoundationServer.
 * 
 * @author Erik Ramfelt
 */
public class Workspaces implements ListWorkspacesCommand.WorkspaceFactory {

    private Map<String,Workspace> workspaces = new HashMap<String,Workspace>();
    private Server server;
    private boolean mapIsPopulatedFromServer;

    public Workspaces(Server server) {
        this.server = server;
    }

    /**
     * Get the list of workspaces from the server
     * @return the list of workspaces at the server
     */
    private List<Workspace> getListFromServer() {
        // the 2nd arg must NOT be provided, to force computerName resolution on the agent & not the master
        ListWorkspacesCommand command = new ListWorkspacesCommand(server);
        final List<Workspace> result = server.execute(command.getCallable());
        return result;
    }
    
    /**
     * Populate the map field with workspaces from the server once.
     */
    private void populateMapFromServer() {
        if (!mapIsPopulatedFromServer) {
            for (Workspace workspace : getListFromServer()) {
                workspaces.put(workspace.getName(), workspace);
            }
            mapIsPopulatedFromServer = true;
        }
    }
    
    /**
     * Returns the workspace with the specified name
     * @param workspaceName the name of the workspace name
     * @return the workspace with the specified name; null if it wasnt found
     */
    public Workspace getWorkspace(String workspaceName) {
        if (!workspaces.containsKey(workspaceName)) {
            populateMapFromServer();
        }
        return workspaces.get(workspaceName);
    }

    /**
     * Returns the if the workspace with the specified name exists on the server
     * @param workspaceName the name of the workspace 
     * @return true if the workspace exists on server; false otherwise
     */
    public boolean exists(String workspaceName) {
        if (!workspaces.containsKey(workspaceName)) {
            populateMapFromServer();
        }
        return workspaces.containsKey(workspaceName);
    }

    /**
     * Returns the if the workspace exists on the server
     * @param workspace the workspace 
     * @return true if the workspace exists on server; false otherwise
     */
    public boolean exists(Workspace workspace) {
        return exists(workspace.getName());
    }

    /**
     * Returns the name of the workspace that mapped at the given localPath,
     * if applicable.
     *
     * @param localPath the path where TFVC files would be downloaded
     * @return the workspace name if there exists a mapping;
     *          {@code null} otherwise.
     */
    public String getWorkspaceMapping(final String localPath) {
        final GetWorkspaceMappingCommand command = new GetWorkspaceMappingCommand(server, localPath);
        final String result = server.execute(command.getCallable());
        return result;
    }

    /**
     * Create workspace on server, map it and return a workspace object with the specified name
     * @param workspaceName the name of the new workspace
     * @param serverPath the path in TFVC to map
     * @param cloakedPaths the paths in TFVC to exclude from mapping
     * @param localPath the path in the local filesystem to map
     * @return a workspace
     */
    public Workspace newWorkspace(final String workspaceName, final String serverPath, Collection<String> cloakedPaths, final String localPath) {
        NewWorkspaceCommand command = new NewWorkspaceCommand(server, workspaceName, serverPath, cloakedPaths, localPath);
        server.execute(command.getCallable());
        Workspace workspace = new Workspace(workspaceName);
        workspaces.put(workspaceName, workspace);
        return workspace;
    }

    /**
     * Deletes the workspace from the server
     * @param workspace the workspace to delete
     */
    public void deleteWorkspace(Workspace workspace) {
        DeleteWorkspaceCommand command = new DeleteWorkspaceCommand(server, workspace.getName());
        workspaces.remove(workspace.getName());
        server.execute(command.getCallable());
    }

    public Workspace createWorkspace(String name, String computer, String owner, String comment) {
        return new Workspace(name, computer, owner, comment);
    }
}
