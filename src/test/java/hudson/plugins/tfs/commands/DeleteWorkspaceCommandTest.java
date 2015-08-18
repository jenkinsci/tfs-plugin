package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import hudson.plugins.tfs.model.Server;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.concurrent.Callable;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.when;


public class DeleteWorkspaceCommandTest extends AbstractCallableCommandTest {

    @Test
    public void assertLogging() throws Exception {
        when(server.getUserName()).thenReturn("snd\\user_cp");
        final Workspace[] emptyWorkspaceList = new Workspace[0];
        when(vcc.queryWorkspaces(isA(String.class), Matchers.<String>anyObject(), isA(String.class), isA(WorkspacePermissions.class))).thenReturn(emptyWorkspaceList);
        final DeleteWorkspaceCommand command = new DeleteWorkspaceCommand(server, "TheWorkspaceName", "computerName") {
            @Override
            public Server createServer() {
                return server;
            }
        };
        final Callable<Void> callable = command.getCallable();

        callable.call();

        assertLog(
                "Deleting workspaces named 'TheWorkspaceName' from computer 'computerName'...",
                "Deleted 0 workspace(s) named 'TheWorkspaceName'."
        );
    }
}
