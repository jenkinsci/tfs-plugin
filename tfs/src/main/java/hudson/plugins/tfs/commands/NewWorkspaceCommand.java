//CHECKSTYLE:OFF
package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.path.LocalPath;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolderType;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class NewWorkspaceCommand extends AbstractCallableCommand<Void, Exception> {

    private static final WorkingFolder[] EMPTY_WORKING_FOLDER_ARRAY = new WorkingFolder[0];
    private static final String CloakingTemplate = "Cloaking '%s' in workspace '%s'...";
    private static final String CreatingTemplate = "Creating workspace '%s' owned by '%s'...";
    private static final String CreatedTemplate = "Created workspace '%s'.";
    private static final String MappingTemplate = "Mapping '%s' to local folder '%s' in workspace '%s'...";

    private final String workspaceName;
    private final String serverPath;
    private final Collection<String> cloakedPaths;
    private final String localPath;

    public NewWorkspaceCommand(final ServerConfigurationProvider server, final String workspaceName, final String serverPath, Collection<String> cloakedPaths, final String localPath) {
        super(server);
        this.workspaceName = workspaceName;
        this.serverPath = serverPath;
        this.cloakedPaths = cloakedPaths;
        this.localPath = localPath;
    }

    public Callable<Void, Exception> getCallable() {
        return this;
    }

    public Void call() throws IOException {
        final Server server = createServer();
        final MockableVersionControlClient vcc = server.getVersionControlClient();
        final TFSTeamProjectCollection connection = vcc.getConnection();
        final TaskListener listener = server.getListener();
        final PrintStream logger = listener.getLogger();
        final String userName = server.getUserName();

        final String creatingMessage = String.format(CreatingTemplate, workspaceName, userName);
        logger.println(creatingMessage);
        
        WorkingFolder[] foldersToMap = null;
        if (serverPath != null && localPath != null) {
            final String mappingMessage = String.format(MappingTemplate, serverPath, localPath, workspaceName);
            logger.println(mappingMessage);

            final List<WorkingFolder> folderList = new ArrayList<WorkingFolder>();

            folderList.add(new WorkingFolder(serverPath, LocalPath.canonicalize(localPath), WorkingFolderType.MAP, RecursionType.FULL));


            for (final String cloakedPath : cloakedPaths) {
                final String cloakingMessage = String.format(CloakingTemplate, cloakedPath, workspaceName);
                logger.println(cloakingMessage);

                folderList.add(new WorkingFolder(cloakedPath, null, WorkingFolderType.CLOAK));
            }
            foldersToMap = folderList.toArray(EMPTY_WORKING_FOLDER_ARRAY);
        }

        updateCache(connection);
        // TODO: we might need to delete a previous workspace that had another name
        vcc.createWorkspace(
                foldersToMap,
                workspaceName,
                VersionControlConstants.AUTHENTICATED_USER,
                VersionControlConstants.AUTHENTICATED_USER,
                null /* TODO: set comment to something nice/useful */,
                WorkspaceLocation.SERVER /* TODO: pull request #33 adds LOCAL support */,
                WorkspaceOptions.NONE
        );

        final String createdMessage = String.format(CreatedTemplate, workspaceName);
        logger.println(createdMessage);

        return null;
    }
}
