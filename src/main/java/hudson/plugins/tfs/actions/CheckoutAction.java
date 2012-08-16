package hudson.plugins.tfs.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hudson.FilePath;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.ProjectData;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;
import hudson.plugins.tfs.util.DateUtil;

public class CheckoutAction {

    private final String workspaceName;
    private final ProjectData projects[];
    private final boolean useUpdate;

    public CheckoutAction(String workspaceName, ProjectData projects[], boolean useUpdate) {
        this.workspaceName = workspaceName;
        this.projects = projects;
        this.useUpdate = useUpdate;
    }

    public List<ChangeSet> checkout(Server server, FilePath workspacePath, Calendar lastBuildTimestamp, Calendar currentBuildTimestamp) throws IOException, InterruptedException, ParseException {
        
    	List<ChangeSet> result = new ArrayList<ChangeSet>();
    	
        Workspaces workspaces = server.getWorkspaces();
        if (workspaces.exists(workspaceName) && !useUpdate) {
            Workspace workspace = workspaces.getWorkspace(workspaceName);
            workspaces.deleteWorkspace(workspace);
        }
        
        Workspace workspace;
        if (! workspaces.exists(workspaceName)) {
            for (int ndx = 0; ndx < projects.length; ++ndx) {
                FilePath localFolderPath = workspacePath.child(projects[ndx].getLocalPath());
            if (!useUpdate && localFolderPath.exists()) {
                localFolderPath.deleteContents();
            }
            }
            
            workspace = workspaces.newWorkspace(workspaceName);
            
            // TFS always creates a default, unused "empty" mapping when a new workspace is created - remove it
            workspace.unmapWorkfolder("$/");
            
            for (int ndx = 0; ndx < projects.length; ++ndx) {
                workspace.mapWorkfolder(server.getProject(projects[ndx].getProjectPath()), projects[ndx].getLocalPath());
            }
            
        } else {
            workspace = workspaces.getWorkspace(workspaceName);
        }
        
        for (int ndx = 0; ndx < projects.length; ++ndx) {
            Project project = server.getProject(projects[ndx].getProjectPath());
            
            project.getFiles(projects[ndx].getLocalPath(), "D" + DateUtil.TFS_DATETIME_FORMATTER.get().format(currentBuildTimestamp.getTime()));
        
        if (lastBuildTimestamp != null) {
            	result.addAll(project.getDetailedHistory(lastBuildTimestamp, currentBuildTimestamp));
            }
        }
        
        return result;
    }
}
