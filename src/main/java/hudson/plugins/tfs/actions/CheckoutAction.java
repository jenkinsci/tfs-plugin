package hudson.plugins.tfs.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
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
    private final String localFolder;
    private final boolean useUpdate;

    public CheckoutAction(String workspaceName, String projectPath, String localFolder, boolean useUpdate) {
        this.workspaceName = workspaceName;
        this.projectPath = projectPath;
        this.localFolder = localFolder;
        this.useUpdate = useUpdate;
    }

    public List<ChangeSet> checkout(Server server, FilePath workspacePath, Calendar lastBuildTimestamp, Calendar currentBuildTimestamp) throws IOException, InterruptedException, ParseException {
        
        Workspaces workspaces = server.getWorkspaces();
        Project project = server.getProject(projectPath);
        
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
            workspace.mapWorkfolder(project, localFolder);
        } else {
            workspace = workspaces.getWorkspace(workspaceName);
        }
        
        project.getFiles(localFolder, "D" + DateUtil.TFS_DATETIME_FORMATTER.get().format(currentBuildTimestamp.getTime()));
        
        if (lastBuildTimestamp != null) {
            return project.getDetailedHistory(lastBuildTimestamp, currentBuildTimestamp);
        }
        
        return new ArrayList<ChangeSet>();
    }
}
