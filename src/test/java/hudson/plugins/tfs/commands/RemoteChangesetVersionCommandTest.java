package hudson.plugins.tfs.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.StringReader;
import java.util.Calendar;

import org.junit.Test;

public class RemoteChangesetVersionCommandTest {

    private static final Calendar fixedPointInTime = Util.getCalendar(2013, 07, 02, 15, 40, 50);
    
    @Test
    public void assertArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https://tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        MaskedArgumentListBuilder arguments = new RemoteChangesetVersionCommand(config, "$/tfsandbox", fixedPointInTime).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("history $/tfsandbox -recursive -stopafter:1 -noprompt -version:~D2013-07-02T15:40:51Z -format:brief -login:snd\\user_cp,password -server:https://tfs02.codeplex.com", arguments.toStringWithQuote());
    }

    @Test
    public void assertNoChangesWithEmptyOutput() throws Exception {
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", fixedPointInTime);
        String changesetNumber = command.parse(new StringReader(""));
        assertEquals("Change set number was incorrect", "", changesetNumber);
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", fixedPointInTime);
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
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25 changed and created one\n");
        
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", fixedPointInTime);
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
        
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", fixedPointInTime);
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12495", changesetNumber);
    }    

    @Test
    public void assertChangesNoEmptyLine() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "12497     SND\\redsolo_cp 2008-jun-27 13:21:25\n");
        
        RemoteChangesetVersionCommand command = new RemoteChangesetVersionCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", fixedPointInTime);
        String changesetNumber = command.parse(reader);
        assertEquals("Change set number was incorrect", "12497", changesetNumber);
    }    

}
