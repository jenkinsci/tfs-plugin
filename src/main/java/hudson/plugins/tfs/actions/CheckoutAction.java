package hudson.plugins.tfs.actions;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.FilePath;
import hudson.plugins.tfs.commands.RemoteChangesetVersionCommand;
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

    public List<ChangeSet> checkout(Server server, FilePath workspacePath, Calendar lastBuildTimestamp, Calendar currentBuildTimestamp) throws IOException, InterruptedException, ParseException {

        final VersionSpec lastBuildVersionSpec;
        if (lastBuildTimestamp != null) {
            lastBuildVersionSpec = new DateVersionSpec(lastBuildTimestamp);
        }
        else{
            lastBuildVersionSpec = null;
        }

        final VersionSpec currentBuildVersionSpec = new DateVersionSpec(currentBuildTimestamp);

        return checkout(server, workspacePath, lastBuildVersionSpec, currentBuildVersionSpec);
    }

    public List<ChangeSet> checkout(final Server server, final FilePath workspacePath, final VersionSpec lastBuildVersionSpec, final VersionSpec currentBuildVersionSpec) throws IOException, InterruptedException {

        Project project = getProject(server, workspacePath);

        final String versionSpecString = RemoteChangesetVersionCommand.toString(currentBuildVersionSpec);
        project.getFiles(localFolder, versionSpecString);

        if (lastBuildVersionSpec != null) {
            return project.getVCCHistory(lastBuildVersionSpec, currentBuildVersionSpec, true, Integer.MAX_VALUE);
        }

        return new ArrayList<ChangeSet>();
    }

    public List<ChangeSet> checkoutBySingleVersionSpec(Server server, FilePath workspacePath, String singleVersionSpec) throws IOException, InterruptedException {
        Project project = getProject(server, workspacePath);
        project.getFiles(localFolder, singleVersionSpec);

        return project.getDetailedHistory(singleVersionSpec);
    }

    static String determineCheckoutPath(final FilePath workspacePath, final String localFolder) {
        final FilePath combinedPath = new FilePath(workspacePath, localFolder);
        final String result = combinedPath.getRemote();
        return result;
    }

    private Project getProject(Server server, FilePath workspacePath)
			throws IOException, InterruptedException {
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
            workspace.mapWorkfolder(project, localFolderPath.getRemote());
        } else {
            workspace = workspaces.getWorkspace(workspaceName);
        }
		return project;
	}

}
