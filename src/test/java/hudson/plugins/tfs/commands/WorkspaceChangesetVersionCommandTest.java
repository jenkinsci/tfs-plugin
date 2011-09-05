package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.StringReader;

import org.junit.Test;

public class WorkspaceChangesetVersionCommandTest {
    
    @Test
    public void assertBriefHistoryArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new WorkspaceChangesetVersionCommand(config, "$/tfsandbox", "workspace_name", "owner").getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("history $/tfsandbox -recursive -stopafter:1 -noprompt -version:Wworkspace_name;owner -format:brief -login:snd\\user_cp,password", arguments.toStringWithQuote());
    }

    @Test
    public void assertNoChangesWithEmptyOutput() throws Exception {
        WorkspaceChangesetVersionCommand command = new WorkspaceChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", "workspace_name", "owner");
        String changesetNumber = command.parse(new StringReader(""));
        assertEquals("Change set number was incorrect", "", changesetNumber);
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        WorkspaceChangesetVersionCommand command = new WorkspaceChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", "workspace_name", "owner");
        StringReader reader = new StringReader("No history entries were found for the item and version combination specified.\n\n");
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "", changesetNumber);
    }    
    
    @Test
    public void assertChangesWithChangeOutput() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25 changed and created one\n" +
                "12493     SND\\redsolo_cp 2008-jun-27 13:19:41 changed and created one\n" +
                "12492     SND\\redsolo_cp 2008-jun-27 13:11:15 first file\n" +
                "12472     RNO\\_MCLWEB    2008-jun-27 11:16:06 Created team project folder $/tfsandbox via the Team Project Creation Wizard\n");
        
        WorkspaceChangesetVersionCommand command = new WorkspaceChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", "workspace_name", "owner");
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12495", changesetNumber);
    }    
    
    @Test
    public void assertChangesWithNoComment() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25\n");
        
        WorkspaceChangesetVersionCommand command = new WorkspaceChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", "workspace_name", "owner");
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12495", changesetNumber);
    }    

    @Test
    public void assertChangesNoEmptyLine() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "12497     SND\\redsolo_cp 2008-jun-27 13:21:25\n");
        
        WorkspaceChangesetVersionCommand command = new WorkspaceChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", "workspace_name", "owner");
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12497", changesetNumber);
    }    
    
}
