package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.VersionControlConstants;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.jni.helpers.LocalHost;
import hudson.model.TaskListener;
import hudson.plugins.tfs.model.MockableVersionControlClient;
import hudson.plugins.tfs.model.Server;

import java.io.PrintStream;
import java.util.concurrent.Callable;

public class DeleteWorkspaceCommand extends AbstractCallableCommand {

    private static final String DeletingTemplate = "Deleting workspaces named '%s' from computer '%s'...";
    private static final String DeletedTemplate = "Deleted %d workspace(s) named '%s'.";

    private final String workspaceName;
    private final String computerName;

    public DeleteWorkspaceCommand(final Server server, final String workspaceName) {
        this(server, workspaceName, null);
    }

    public DeleteWorkspaceCommand(final Server server, final String workspaceName, final String computerName) {
        super(server);
        this.workspaceName = workspaceName;
        this.computerName = computerName;
    }

    public Callable<Void> getCallable() {
        return new Callable<Void>() {
            public Void call() {
                final Server server = getServer();
                final MockableVersionControlClient vcc = server.getVersionControlClient();
                final TaskListener listener = server.getListener();
                final PrintStream logger = listener.getLogger();

                final String computerName = (DeleteWorkspaceCommand.this.computerName == null)
                        ? LocalHost.getShortName()
                        : DeleteWorkspaceCommand.this.computerName;
                final String deletingMessage = String.format(DeletingTemplate, workspaceName, computerName);
                logger.println(deletingMessage);

                final WorkspacePermissions filter = WorkspacePermissions.NONE_OR_NOT_SUPPORTED;
                final Workspace[] workspaces = vcc.queryWorkspaces(workspaceName, null, computerName, filter);
                int numDeletions = 0;
                for (final Workspace innerWorkspace : workspaces) {
                    vcc.deleteWorkspace(innerWorkspace);
                    numDeletions++;
                }

                final String deletedMessage = String.format(DeletedTemplate, numDeletions, workspaceName);
                logger.println(deletedMessage);

                return null;
            }
        };
    }
}
