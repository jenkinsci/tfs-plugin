package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.Callable;


public class NewWorkspaceCommand extends AbstractCallableCommand {

    private static final String CreatingTemplate = "Creating workspace '%s' owned by '%s'...";
    private static final String CreatedTemplate = "Created workspace '%s'.";
    private static final String MappingTemplate = "Mapping '%s' to local folder '%s' in workspace '%s'...";
    private static final String MappedTemplate = "Mapped '%s' to local folder '%s' in workspace '%s'.";

    private final String workspaceName;
    private final String serverPath;
    private final String localPath;

    public NewWorkspaceCommand(final Server server, final String workspaceName, final String serverPath, final String localPath) {
        super(server);
        this.workspaceName = workspaceName;
        this.serverPath = serverPath;
        this.localPath = localPath;
    }

    public Callable<Void> getCallable() {
        return new Callable<Void>() {
            public Void call() throws IOException {
                final Server server = createServer();
                final MockableVersionControlClient vcc = server.getVersionControlClient();
                final TaskListener listener = server.getListener();
                final PrintStream logger = listener.getLogger();
                final String userName = server.getUserName();

                final String creatingMessage = String.format(CreatingTemplate, workspaceName, userName);
                logger.println(creatingMessage);

                final Workspace workspace = vcc.createWorkspace(
                        null,
                        workspaceName,
                        VersionControlConstants.AUTHENTICATED_USER,
                        VersionControlConstants.AUTHENTICATED_USER,
                        null /* TODO: set comment to something nice/useful */,
                        WorkspaceLocation.SERVER /* TODO: pull request #33 adds LOCAL support */,
                        WorkspaceOptions.NONE
                );

                final String createdMessage = String.format(CreatedTemplate, workspaceName);
                logger.println(createdMessage);

                if (serverPath != null && localPath != null) {
                    final String mappingMessage = String.format(MappingTemplate, serverPath, localPath, workspaceName);
                    logger.println(mappingMessage);

                    workspace.map(serverPath, localPath);

                    final String mappedMessage = String.format(MappedTemplate, serverPath, localPath, workspaceName);
                    logger.println(mappedMessage);
                }

                return null;
            }
        };
    }
}
