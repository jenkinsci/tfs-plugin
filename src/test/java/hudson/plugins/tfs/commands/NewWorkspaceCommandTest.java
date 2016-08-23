package hudson.plugins.tfs.commands;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceOptions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import hudson.plugins.tfs.model.Server;
import hudson.remoting.Callable;
import org.junit.Test;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

public class NewWorkspaceCommandTest extends AbstractCallableCommandTest {

    private static final List<String> EMPTY_CLOAKED_PATHS = Collections.emptyList();

    @Test public void assertLogging() throws Exception {
        when(server.getUserName()).thenReturn("snd\\user_cp");
        when(vcc.createWorkspace(aryEq((WorkingFolder[]) null),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(WorkspaceLocation.class),
                isA(WorkspaceOptions.class))).thenReturn(null);
        final NewWorkspaceCommand command = new NewWorkspaceCommand(server, "TheWorkspaceName", null, EMPTY_CLOAKED_PATHS, null) {
            @Override
            public Server createServer() {
                return server;
            }

            @Override
            protected void updateCache(final TFSTeamProjectCollection connection) {
                // no-op for tests
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
        final List<String> cloakedPaths = Collections.singletonList("$/Stuff/Hide");

        when(server.getUserName()).thenReturn("snd\\user_cp");
        when(vcc.createWorkspace(aryEq((WorkingFolder[]) null),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(String.class),
                isA(WorkspaceLocation.class),
                isA(WorkspaceOptions.class))).thenReturn(null);
        final NewWorkspaceCommand command = new NewWorkspaceCommand(server, "TheWorkspaceName", "$/Stuff", cloakedPaths, "/home/jenkins/jobs/stuff/workspace") {
            @Override
            public Server createServer() {
                return server;
            }

            @Override
            protected void updateCache(final TFSTeamProjectCollection connection) {
                // no-op for tests
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
        return new NewWorkspaceCommand(serverConfig, "workspaceName", "$/serverPath", EMPTY_CLOAKED_PATHS, "local/path");
    }
}
