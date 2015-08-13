package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import org.junit.Test;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.when;


public class DeleteWorkspaceCommandTest extends AbstractCallableCommandTest {

    @Test
    public void assertLogging() throws Exception {
        when(server.getUserName()).thenReturn("snd\\user_cp");
        when(vcc.queryWorkspace(isA(String.class), isA(String.class))).thenReturn(null);
        doNothing().when(vcc).deleteWorkspace(isA(Workspace.class));
        final DeleteWorkspaceCommand command = new DeleteWorkspaceCommand(server, "TheWorkspaceName");
        final Callable<Void> callable = command.getCallable();

        callable.call();

        assertLog(
                "Deleting workspace 'TheWorkspaceName' owned by 'snd\\user_cp'...",
                "Deleted workspace 'TheWorkspaceName'."
        );
    }
}
