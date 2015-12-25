package hudson.plugins.tfs.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hudson.plugins.tfs.commands.DeleteWorkspaceCommand;
import hudson.plugins.tfs.commands.ListWorkspacesCommand;
import hudson.plugins.tfs.commands.NewWorkspaceCommand;

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
    private List<Workspace> getListFromServer(boolean showWorkspaces) {
        ListWorkspacesCommand command = new ListWorkspacesCommand(server, showWorkspaces);
        final List<Workspace> result = server.execute(command.getCallable());
        return result;
    }
    
    /**
     * Populate the map field with workspaces from the server once.
     */
    private void populateMapFromServer(boolean showWorkspaces) {
        if (!mapIsPopulatedFromServer) {
            for (Workspace workspace : getListFromServer(showWorkspaces)) {
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
    public Workspace getWorkspace(String workspaceName, boolean showWorkspaces) {
        if (!workspaces.containsKey(workspaceName)) {
            populateMapFromServer(showWorkspaces);
        }
        return workspaces.get(workspaceName);
    }

    /**
     * Returns the if the workspace with the specified name exists on the server
     * @param workspaceName the name of the workspace 
     * @return true if the workspace exists on server; false otherwise
     */
    public boolean exists(String workspaceName, boolean showWorkspaces) {
        if (!workspaces.containsKey(workspaceName)) {
            populateMapFromServer(showWorkspaces);
        }
        return workspaces.containsKey(workspaceName);
    }

    /**
     * Returns the if the workspace exists on the server
     * @param workspace the workspace 
     * @return true if the workspace exists on server; false otherwise
     */
    public boolean exists(Workspace workspace, boolean showWorkspaces) {
        return exists(workspace.getName(), showWorkspaces);
    }

    /**
     * Create workspace on server, map it and return a workspace object with the specified name
     * @param workspaceName the name of the new workspace
     * @param serverPath the path in TFVC to map
     * @param localPath the path in the local filesystem to map
     * @return a workspace
     */
    public Workspace newWorkspace(final String workspaceName, final String serverPath, final String localPath) {
        NewWorkspaceCommand command = new NewWorkspaceCommand(server, workspaceName, serverPath, localPath);
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
