package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;
import org.junit.Test;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

public class NewWorkspaceCommandTest extends AbstractCallableCommandTest {
    
    @Test public void assertLogging() throws Exception {
        when(server.getUserName()).thenReturn("snd\\user_cp");
        when(vcc.createWorkspace(aryEq((WorkingFolder[]) null),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(WorkspaceLocation.class),
                isA(WorkspaceOptions.class))).thenReturn(null);
        final NewWorkspaceCommand command = new NewWorkspaceCommand(server, "TheWorkspaceName", null, new ArrayList<String>(), null) {
            @Override
            public Server createServer() {
                return server;
            }
        };
        final Callable<Void, Exception> callable = command.getCallable();

        callable.call();

        assertLog(
                "Creating workspace 'TheWorkspaceName' owned by 'snd\\user_cp'...",
                "Created workspace 'TheWorkspaceName'."
        );
    }

    @Test public void assertLoggingWhenAlsoMapping() throws Exception {
        List<String> cloakPaths = new ArrayList<String>();
        cloakPaths.add("$/Stuff/Hide");

        when(server.getUserName()).thenReturn("snd\\user_cp");
        when(vcc.createWorkspace(aryEq((WorkingFolder[]) null),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(WorkspaceLocation.class),
                isA(WorkspaceOptions.class))).thenReturn(null);
        final NewWorkspaceCommand command = new NewWorkspaceCommand(server, "TheWorkspaceName", "$/Stuff", cloakPaths, "/home/jenkins/jobs/stuff/workspace") {
            @Override
            public Server createServer() {
                return server;
            }
        };
        final Callable<Void, Exception> callable = command.getCallable();

        callable.call();

        assertLog(
                "Creating workspace 'TheWorkspaceName' owned by 'snd\\user_cp'...",
                "Mapping '$/Stuff' to local folder '/home/jenkins/jobs/stuff/workspace' in workspace 'TheWorkspaceName'...",
                "Cloaking '$/Stuff/Hide' in workspace 'TheWorkspaceName'...",
                "Created workspace 'TheWorkspaceName'."
        );
    }

    @Override protected AbstractCallableCommand createCommand(final ServerConfigurationProvider serverConfig) {
        return new NewWorkspaceCommand(serverConfig, "workspaceName", "$/serverPath", new ArrayList<String>(), "local/path");
    }
}
