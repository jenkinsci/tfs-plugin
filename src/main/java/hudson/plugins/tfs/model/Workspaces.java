package hudson.plugins.tfs.model;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

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
    private List<Workspace> getListFromServer() throws IOException, InterruptedException {
        ListWorkspacesCommand command = new ListWorkspacesCommand(this, server);
        Reader reader = null;
        try {
            reader = server.execute(command.getArguments());
            return command.parse(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
    
    /**
     * Populate the map field with workspaces from the server once.
     */
    private void populateMapFromServer() throws IOException, InterruptedException {
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
    public Workspace getWorkspace(String workspaceName) throws IOException, InterruptedException {
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
    public boolean exists(String workspaceName) throws IOException, InterruptedException {
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
    public boolean exists(Workspace workspace) throws IOException, InterruptedException {
        return exists(workspace.getName());
    }

    /**
     * Create workspace on server and return a workspace object with the specified name
     * @param name the name of the new workspace
     * @return a workspace
     */
    public Workspace newWorkspace(String name) throws IOException, InterruptedException {
        NewWorkspaceCommand command = new NewWorkspaceCommand(server, name);
        server.execute(command.getArguments()).close();        
        Workspace workspace = new Workspace(server, name);
        workspaces.put(name, workspace);
        return workspace;
    }
    
    /**
     * Deletes the workspace from the server
     * @param workspace the workspace to delete
     */
    public void deleteWorkspace(Workspace workspace) throws IOException, InterruptedException {
        DeleteWorkspaceCommand command = new DeleteWorkspaceCommand(server, workspace.getName());
        workspaces.remove(workspace.getName());
        server.execute(command.getArguments()).close();
    }

    public Workspace createWorkspace(String name, String computer, String owner, String comment) {
        return new Workspace(server, name, computer, owner, comment);
    }
}
