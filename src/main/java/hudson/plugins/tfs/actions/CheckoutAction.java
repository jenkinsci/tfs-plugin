package hudson.plugins.tfs.actions;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.plugins.tfs.commands.RemoteChangesetVersionCommand;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

import java.io.IOException;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class CheckoutAction {

    private final String workspaceName;
    private final String projectPath;
    private final Collection<String> cloakedPaths;
    private final String localFolder;
    private final boolean useUpdate;

    public CheckoutAction(String workspaceName, String projectPath, Collection<String> cloakedPaths, String localFolder, boolean useUpdate) {
        this.workspaceName = workspaceName;
        this.projectPath = projectPath;
        this.cloakedPaths = cloakedPaths;
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
        final String normalizedFolder = determineCheckoutPath(workspacePath, localFolder);
        project.getFiles(normalizedFolder, versionSpecString);

        if (lastBuildVersionSpec != null) {
            return project.getVCCHistory(lastBuildVersionSpec, currentBuildVersionSpec, true, Integer.MAX_VALUE);
        }

        return new ArrayList<ChangeSet>();
    }

    public List<ChangeSet> checkoutBySingleVersionSpec(Server server, FilePath workspacePath, String singleVersionSpec) throws IOException, InterruptedException {
        Project project = getProject(server, workspacePath);
        final String normalizedFolder = determineCheckoutPath(workspacePath, localFolder);
        project.getFiles(normalizedFolder, singleVersionSpec);

        return project.getDetailedHistory(singleVersionSpec);
    }

    static String determineCheckoutPath(final FilePath workspacePath, final String localFolder) {
        final FilePath combinedPath = new FilePath(workspacePath, localFolder);
        final String result = combinedPath.getRemote();
        return result;
    }

    private Project getProject(final Server server, final FilePath workspacePath)
            throws IOException, InterruptedException {
        final Workspaces workspaces = server.getWorkspaces();
        final Project project = server.getProject(projectPath);
        final FilePath localFolderPath = workspacePath.child(localFolder);
        final String localPath = localFolderPath.getRemote();
        final TaskListener listener = server.getListener();
        final PrintStream logger = listener.getLogger();

        boolean shouldDelete = false;
        if (workspaces.exists(workspaceName)) {
            boolean localFolderExists = true;
            final boolean isMapped = workspaces.isMapped(localPath);
            if (!isMapped) {
                logger.println("Warning: Although the server thinks the workspace exists, no mapping was found.");
            }
            else {
                localFolderExists = localFolderPath.exists();
                if (!localFolderExists) {
                    logger.println("Warning: The local folder is missing.");
                }

            }
            if (!localFolderExists || !isMapped || !useUpdate) {
                shouldDelete = true;
                Workspace workspace = workspaces.getWorkspace(workspaceName);
                workspaces.deleteWorkspace(workspace);
            }
        }

        Workspace workspace;
        if (! workspaces.exists(workspaceName)) {
            if (shouldDelete && localFolderPath.exists()) {
                localFolderPath.deleteContents();
            }
            final String serverPath = project.getProjectPath();
            workspace = workspaces.newWorkspace(workspaceName, serverPath, cloakedPaths, localPath);
        } else {
            workspace = workspaces.getWorkspace(workspaceName);
        }
        return project;
	}

}
