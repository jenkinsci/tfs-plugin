package hudson.plugins.tfs.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import hudson.FilePath;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;
import hudson.plugins.tfs.util.DateUtil;

public class CheckoutAction {

    private final String workspaceName;
    private final String projectPath;
    private Collection<String> cloakPaths;
    private final String localFolder;
    private final boolean useUpdate;

    public CheckoutAction(String workspaceName, String projectPath, Collection<String> cloakPaths, String localFolder, boolean useUpdate) {
        this.workspaceName = workspaceName;
        this.projectPath = projectPath;
        this.cloakPaths = cloakPaths;
        this.localFolder = localFolder;
        this.useUpdate = useUpdate;
    }

    public List<ChangeSet> checkout(Server server, FilePath workspacePath, Calendar lastBuildTimestamp, Calendar currentBuildTimestamp) throws IOException, InterruptedException, ParseException {
        
        Project project = getProject(server, workspacePath);
        
        project.getFiles(localFolder, "D" + DateUtil.TFS_DATETIME_FORMATTER.get().format(currentBuildTimestamp.getTime()));
        
        if (lastBuildTimestamp != null) {
            return project.getDetailedHistory(lastBuildTimestamp, currentBuildTimestamp);
        }
        
        return new ArrayList<ChangeSet>();
    }

    public List<ChangeSet> checkoutByLabel(Server server, FilePath workspacePath, String label) throws IOException, InterruptedException {
    	Project project = getProject(server, workspacePath);
    	project.getFiles(localFolder, label);
    	
    	return project.getDetailedHistory(label);
    }

    private Project getProject(Server server, FilePath workspacePath)
			throws IOException, InterruptedException {
		Workspaces workspaces = server.getWorkspaces();
        Project project = server.getProject(projectPath, cloakPaths);
        
        if (workspaces.exists(workspaceName) && !useUpdate) {
            Workspace workspace = workspaces.getWorkspace(workspaceName);
            workspaces.deleteWorkspace(workspace);
        }
        
        Workspace workspace;
        if (! workspaces.exists(workspaceName)) {
            FilePath localFolderPath = workspacePath.child(localFolder);
            if (!useUpdate && localFolderPath.exists()) {
                localFolderPath.deleteContents();
            }
            workspace = workspaces.newWorkspace(workspaceName);
            workspace.mapWorkfolder(project, localFolderPath.getRemote());
        } else {
            workspace = workspaces.getWorkspace(workspaceName);
        }
		return project;
	}

}
