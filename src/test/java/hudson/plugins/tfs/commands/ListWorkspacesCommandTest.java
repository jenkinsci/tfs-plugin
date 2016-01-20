package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.VersionControlClient;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissions;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import hudson.plugins.tfs.IntegrationTestHelper;
import hudson.plugins.tfs.IntegrationTests;
import hudson.plugins.tfs.model.NativeLibraryManager;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;

import hudson.remoting.Callable;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Bug;

public class ListWorkspacesCommandTest extends AbstractCallableCommandTest {

    @Category(IntegrationTests.class)
    @Test public void assertLoggingWithComputer() throws Exception {
        final IntegrationTestHelper helper = new IntegrationTestHelper();
        final String serverUrl = helper.getServerUrl();
        final URI serverUri = URI.create(serverUrl);
        NativeLibraryManager.initialize();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                helper.getUserName(), helper.getUserPassword());
        final TFSTeamProjectCollection tpc = new TFSTeamProjectCollection(serverUri, credentials);

        try {
            final VersionControlClient vcc = tpc.getVersionControlClient();
            final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace[] workspaces
                    = new com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace[1];
            workspaces[0] = new com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace(
                    vcc,
                    "Hudson.JOBXXXXXXXXXXXXXX",
                    "First.LastXX",
                    "This is a comment",
                    null,
                    "XXXX-XXXX-007",
                    WorkspaceLocation.SERVER
            );
            when(server.getUrl()).thenReturn("http://tfs.invalid:8080/tfs/DefaultCollection/");
            when(this.vcc.queryWorkspaces(null, null, "XXXX-XXXX-007", WorkspacePermissions.NONE_OR_NOT_SUPPORTED))
                    .thenReturn(workspaces);
            final ListWorkspacesCommand command = new ListWorkspacesCommand(server, "XXXX-XXXX-007", true) {
                @Override
                public Server createServer() {
                    return server;
                }
            };
            final Callable<List<Workspace>, Exception> callable = command.getCallable();

            callable.call();

            assertLog(
                "Listing workspaces from http://tfs.invalid:8080/tfs/DefaultCollection/...",
                "Workspace                Owner        Computer      Comment          ",
                "------------------------ ------------ ------------- -----------------",
                "Hudson.JOBXXXXXXXXXXXXXX First.LastXX XXXX-XXXX-007 This is a comment"
            );
        } finally {
            tpc.close();
        }
    }

    @Category(IntegrationTests.class)
    @Test public void assertLoggingWithoutComputer() throws Exception {
        final IntegrationTestHelper helper = new IntegrationTestHelper();
        final String serverUrl = helper.getServerUrl();
        final URI serverUri = URI.create(serverUrl);
        NativeLibraryManager.initialize();
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
                helper.getUserName(), helper.getUserPassword());
        final TFSTeamProjectCollection tpc = new TFSTeamProjectCollection(serverUri, credentials);

        try {
            final VersionControlClient vcc = tpc.getVersionControlClient();
            final com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace[] workspaces
                    = new com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace[1];
            workspaces[0] = new com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace(
                    vcc,
                    "Hudson.JOBXXXXXXXXXXXXXX",
                    "First.LastXX",
                    "This is a comment",
                    null,
                    "XXXX-XXXX-007",
                    WorkspaceLocation.SERVER
            );
            when(server.getUrl()).thenReturn("http://tfs.invalid:8080/tfs/DefaultCollection/");
            when(this.vcc.queryWorkspaces(null, null, null, WorkspacePermissions.NONE_OR_NOT_SUPPORTED))
                    .thenReturn(workspaces);
            final ListWorkspacesCommand command = new ListWorkspacesCommand(server, null, true) {
                @Override
                public Server createServer() {
                    return server;
                }
            };
            final Callable<List<Workspace>, Exception> callable = command.getCallable();

            callable.call();

            assertLog(
                    "Listing workspaces from http://tfs.invalid:8080/tfs/DefaultCollection/...",
                    "Workspace                Owner        Computer      Comment          ",
                    "------------------------ ------------ ------------- -----------------",
                    "Hudson.JOBXXXXXXXXXXXXXX First.LastXX XXXX-XXXX-007 This is a comment"
            );
        } finally {
            tpc.close();
        }
    }

    @Test
    public void assertEmptyListWithEmptyOutput() throws Exception {
        ListWorkspacesCommand command = new ListWorkspacesCommand(mock(Server.class), true);
        List<Workspace> list = command.parse(new StringReader(""));
        assertNotNull("List can not be null", list);
        assertEquals("Number of workspaces was incorrect", 0, list.size());
    }

    @Test
    public void assertListWithValidOutput() throws Exception {
        StringReader reader = new StringReader(
                "Server: https://tfs02.codeplex.com/\n" +
                "Workspace Owner          Computer Comment\n" +
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "asterix2  SND\\redsolo_cp ASTERIX\n" +
                "astreix   SND\\redsolo_cp ASTERIX  This is a comment\n");
        
        ListWorkspacesCommand command = new ListWorkspacesCommand(
                mock(Server.class), true);
        List<Workspace> list = command.parse(reader);
        assertNotNull("List can not be null", list);
        assertEquals("Number of workspaces was incorrect", 2, list.size());
        Workspace workspace = list.get(0);
        assertEquals("The workspace name is incorrect", "asterix2", workspace.getName());
        assertEquals("The owner name is incorrect", "SND\\redsolo_cp", workspace.getOwner());
        assertEquals("The computer name is incorrect", "ASTERIX", workspace.getComputer());
        workspace = list.get(1);
        assertEquals("The workspace name is incorrect", "astreix", workspace.getName());
        assertEquals("The owner name is incorrect", "SND\\redsolo_cp", workspace.getOwner());
        assertEquals("The computer name is incorrect", "ASTERIX", workspace.getComputer());
        assertEquals("The comment is incorrect", "This is a comment", workspace.getComment());
    }

    @Test
    public void assertListWithWorkspaceContainingSpace() throws Exception {
        StringReader reader = new StringReader(
                "Server: https://tfs02.codeplex.com/\n" +
                "Workspace          Owner      Computer Comment\n" +
                "------------------ ---------- -------- ----------------------------------------\n" +
                "Hudson-node lookup redsolo_cp ASTERIX\n");
        
        ListWorkspacesCommand command = new ListWorkspacesCommand(
                mock(Server.class), true);
        List<Workspace> list = command.parse(reader);
        assertNotNull("List can not be null", list);
        assertEquals("Number of workspaces was incorrect", 1, list.size());
        Workspace workspace = list.get(0);
        assertEquals("The workspace name is incorrect", "Hudson-node lookup", workspace.getName());
    }

    @Bug(4666)
    @Test
    public void assertNoIndexOutOfBoundsIsThrown() throws Exception {

        StringReader reader = new StringReader(
                "Server: teamserver-01\n" +
                "Workspace         Owner  Computer    Comment\n" +
                "----------------- ------ ----------- ------------------------------------------\n" +
                "Hudson-Scrumboard dennis W7-DENNIS-1\n" + 
                "W7-DENNIS-1       dennis W7-DENNIS-1\n");

        new ListWorkspacesCommand(mock(Server.class), true).parse(reader);
    }

    @Bug(4726)
    @Test
    public void assertNoIndexOutOfBoundsIsThrownSecondEdition() throws Exception {

        StringReader reader = new StringReader(
                "Server: xxxx-xxxx-010\n" +
                "Workspace                Owner        Computer      Comment\n" +
                "------------------------ ------------ ------------- ---------------------------\n" +
                "Hudson.JOBXXXXXXXXXXXXXX First.LastXX XXXX-XXXX-007\n");

        new ListWorkspacesCommand(mock(Server.class), true).parse(reader);
    }

    @Test public void logWithNoWorkspaces() throws IOException {

        ListWorkspacesCommand.log(new ArrayList<Workspace>(0), listener.getLogger());

        assertLog(
                "Workspace Owner Computer Comment",
                "--------- ----- -------- -------"
        );
    }

    @Test public void logWithManyWorkspaces() throws IOException {

        final ArrayList<Workspace> workspaces = new ArrayList<Workspace>();
        workspaces.add(new Workspace("Hudson.JOBXXXXXXXXXXXXXX", "XXXX-XXXX-007", "First.LastXX", "This is a comment"));
        workspaces.add(new Workspace("Hudson-newJob-MASTER", "COMPUTER", "jenkins-tfs-plugin", "Created by the Jenkins tfs-plugin functional tests."));

        ListWorkspacesCommand.log(workspaces, listener.getLogger());

        assertLog(
                "Workspace                Owner              Computer      Comment                                            ",
                "------------------------ ------------------ ------------- ---------------------------------------------------",
                "Hudson.JOBXXXXXXXXXXXXXX First.LastXX       XXXX-XXXX-007 This is a comment                                  ",
                "Hudson-newJob-MASTER     jenkins-tfs-plugin COMPUTER      Created by the Jenkins tfs-plugin functional tests."
        );
    }

    @Test public void logWithOneWorkspace() throws IOException {

        final ArrayList<Workspace> workspaces = new ArrayList<Workspace>(1);
        workspaces.add(new Workspace("asterix", "ASTERIX", "redsolo_cp", "This is a comment"));

        ListWorkspacesCommand.log(workspaces, listener.getLogger());

        assertLog(
                "Workspace Owner      Computer Comment          ",
                "--------- ---------- -------- -----------------",
                "asterix   redsolo_cp ASTERIX  This is a comment"
        );
    }

    @Override protected AbstractCallableCommand createCommand(final ServerConfigurationProvider serverConfig) {
        return new ListWorkspacesCommand(serverConfig, "computer", true);
    }
}
