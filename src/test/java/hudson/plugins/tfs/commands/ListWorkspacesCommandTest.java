package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.io.StringReader;
import java.util.List;
import java.util.regex.Matcher;

import hudson.plugins.tfs.commands.ListWorkspacesCommand;
import hudson.plugins.tfs.commands.ListWorkspacesCommand.WorkspaceFactory;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import org.junit.Test;
import org.mockito.Mockito;

public class ListWorkspacesCommandTest {
    
    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUrl()).toReturn("https//tfs02.codeplex.com");
        stub(config.getUserName()).toReturn("snd\\user_cp");
        stub(config.getUserPassword()).toReturn("password");
        
        MaskedArgumentListBuilder arguments = new ListWorkspacesCommand(null,config).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("workspaces /format:brief /server:https//tfs02.codeplex.com /login:snd\\user_cp,password", arguments.toStringWithQuote());
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
                "astreix   SND\\redsolo_cp ASTERIX This is a comment\n");
        
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
    public void assertRegexWorks2() {
        Matcher matcher = ListWorkspacesCommand.BRIEF_WORKSPACE_LIST_PATTERN.matcher("astreix   SND\\redsolo_cp ASTERIX  This is a comment");
        assertTrue(matcher.matches());
        assertEquals(4, matcher.groupCount());
    }
    
    @Test 
    public void assertRegexWorks() {
        Matcher matcher = ListWorkspacesCommand.BRIEF_WORKSPACE_LIST_PATTERN.matcher("astreix   SND\\redsolo_cp ASTERIX");
        assertTrue(matcher.matches());
        assertEquals(4, matcher.groupCount());
    }
}
