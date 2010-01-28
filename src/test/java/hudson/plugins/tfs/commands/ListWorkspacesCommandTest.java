package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.List;

import hudson.plugins.tfs.commands.ListWorkspacesCommand;
import hudson.plugins.tfs.commands.ListWorkspacesCommand.WorkspaceFactory;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.mockito.Mockito;

public class ListWorkspacesCommandTest {

    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new ListWorkspacesCommand(null,config).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workspaces -format:brief -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
    
    @Test
    public void assertArgumentsWithComputer() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new ListWorkspacesCommand(null,config,"akira").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workspaces -format:brief -computer:akira -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertEmptyListWithEmptyOutput() throws Exception {
        ListWorkspacesCommand command = new ListWorkspacesCommand(null, mock(ServerConfigurationProvider.class));
        List<Workspace> list = command.parse(new StringReader(""));
        assertNotNull("List can not be null", list);
        assertEquals("Number of workspaces was incorrect", 0, list.size());
    }

    @Test
    public void assertFactoryIsUsedToCreateWorkspaces() throws Exception {
        WorkspaceFactory factory = Mockito.mock(ListWorkspacesCommand.WorkspaceFactory.class);
        
        StringReader reader = new StringReader(
                "Server: https://tfs02.codeplex.com/\n" +
                "Workspace Owner          Computer Comment\n" +
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "asterix2  SND\\redsolo_cp ASTERIX\n");

        new ListWorkspacesCommand(factory, mock(ServerConfigurationProvider.class)).parse(reader);
        Mockito.verify(factory).createWorkspace("asterix2", "ASTERIX", "SND\\redsolo_cp", "");
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
                new Workspaces(Mockito.mock(Server.class)), 
                mock(ServerConfigurationProvider.class));
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
                new Workspaces(Mockito.mock(Server.class)), 
                mock(ServerConfigurationProvider.class));
        List<Workspace> list = command.parse(reader);
        assertNotNull("List can not be null", list);
        assertEquals("Number of workspaces was incorrect", 1, list.size());
        Workspace workspace = list.get(0);
        assertEquals("The workspace name is incorrect", "Hudson-node lookup", workspace.getName());
    }

    @Bug(4666)
    @Test
    public void assertNoIndexOutOfBoundsIsThrown() throws Exception {
        WorkspaceFactory factory = Mockito.mock(ListWorkspacesCommand.WorkspaceFactory.class);
        
        StringReader reader = new StringReader(
                "Server: teamserver-01\n" +
                "Workspace         Owner  Computer    Comment\n" +
                "----------------- ------ ----------- ------------------------------------------\n" +
                "Hudson-Scrumboard dennis W7-DENNIS-1\n" + 
                "W7-DENNIS-1       dennis W7-DENNIS-1\n");

        new ListWorkspacesCommand(factory, mock(ServerConfigurationProvider.class)).parse(reader);
        Mockito.verify(factory).createWorkspace("W7-DENNIS-1", "W7-DENNIS-1", "dennis", "");
    }

    @Bug(4726)
    @Test
    public void assertNoIndexOutOfBoundsIsThrownSecondEdition() throws Exception {
        WorkspaceFactory factory = Mockito.mock(ListWorkspacesCommand.WorkspaceFactory.class);
        
        StringReader reader = new StringReader(
                "Server: xxxx-xxxx-010\n" +
                "Workspace                Owner        Computer      Comment\n" +
                "------------------------ ------------ ------------- ---------------------------\n" +
                "Hudson.JOBXXXXXXXXXXXXXX First.LastXX XXXX-XXXX-007\n");

        new ListWorkspacesCommand(factory, mock(ServerConfigurationProvider.class)).parse(reader);
        Mockito.verify(factory).createWorkspace("Hudson.JOBXXXXXXXXXXXXXX", "XXXX-XXXX-007", "First.LastXX", "");
    }

}
