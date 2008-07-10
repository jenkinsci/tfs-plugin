package hudson.plugins.tfs.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

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

    public List<ChangeSet> checkout(Server server, Calendar lastBuildTimestamp) throws IOException, InterruptedException, ParseException {
        
        Workspace workspace = new Workspace(server, workspaceName);
        Workspaces workspaces = server.getWorkspaces();
        Project project = server.getProject(projectPath);

        try {
            if (! workspaces.exists(workspace)) {
                workspace = workspaces.newWorkspace(workspaceName);
                workspace.mapWorkfolder(project, localFolder);
            }
            
            project.getFiles(localFolder);
            
            if (lastBuildTimestamp != null) {
                return project.getDetailedHistory(lastBuildTimestamp, Calendar.getInstance());
            }
        } finally {        
            if (!useUpdate) {
                workspaces.deleteWorkspace(workspace);
            }
        }
        
        return new ArrayList<ChangeSet>();
    }
}
