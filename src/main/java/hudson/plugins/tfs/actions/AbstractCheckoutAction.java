package hudson.plugins.tfs.actions;

import hudson.FilePath;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

import java.io.IOException;

public abstract class AbstractCheckoutAction implements CheckoutAction {

    final CheckoutInfo checkoutInfo;

    AbstractCheckoutAction(CheckoutInfo checkoutInfo) {
        this.checkoutInfo = checkoutInfo;
    }

	Project getProject(Server server, FilePath workspacePath)
			throws IOException, InterruptedException {
		Workspaces workspaces = server.getWorkspaces();
        Project project = server.getProject(this.checkoutInfo.getProjectPath());
        
        String workspaceName = this.checkoutInfo.getWorkspaceName();
		boolean useUpdate = this.checkoutInfo.isUseUpdate();
		if (workspaces.exists(workspaceName) && !useUpdate) {
            Workspace workspace = workspaces.getWorkspace(workspaceName);
            workspaces.deleteWorkspace(workspace);
        }
        
        Workspace workspace;
        if (! workspaces.exists(workspaceName)) {
            String localFolder = this.checkoutInfo.getLocalFolder();
			FilePath localFolderPath = workspacePath.child(localFolder);
            if (!useUpdate && localFolderPath.exists()) {
                localFolderPath.deleteContents();
            }
            workspace = workspaces.newWorkspace(workspaceName);
            workspace.mapWorkfolder(project, localFolder);
        } else {
            workspace = workspaces.getWorkspace(workspaceName);
        }
		return project;
	}
	
}
