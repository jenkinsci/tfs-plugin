package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.plugins.tfs.SwedishLocaleTestCase;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.commands.BriefHistoryCommand;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.StringReader;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.jvnet.hudson.test.Bug;

public class BriefHistoryCommandTest extends SwedishLocaleTestCase {
    
    @Bug(6596)
    @Test
    public void assertBriefHistoryArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        Calendar fromTimestamp = Util.getCalendar(2006, 12, 01, 01, 01, 01);
        Calendar toTimestamp = Util.getCalendar(2008, 06, 27, 20, 00, 0);
        
        MaskedArgumentListBuilder arguments = new BriefHistoryCommand(config, "$/tfsandbox", fromTimestamp, toTimestamp).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("history $/tfsandbox -noprompt -version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:01Z -recursive -format:brief -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
        assertEquals(toTimestamp, Util.getCalendar(2008, 06, 27, 20, 00, 0));
    }

    @Test
    public void assertNoChangesWithEmptyOutput() throws Exception {
        BriefHistoryCommand command = new BriefHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Calendar.getInstance(), Calendar.getInstance());
        List<ChangeSet> list = command.parse(new StringReader(""));
        assertEquals("Number of change sets was incorrect", 0, list.size());
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        BriefHistoryCommand command = new BriefHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Calendar.getInstance(), Calendar.getInstance());
        StringReader reader = new StringReader("No history entries were found for the item and version combination specified.\n\n");
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 0, list.size());
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
        
        BriefHistoryCommand command = new BriefHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 06, 01), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 4, list.size());
    }    
    
    @Test
    public void assertChangesWithNoComment() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25\n");
        
        BriefHistoryCommand command = new BriefHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 06, 01), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 1, list.size());
    }    
    
    @Test
    public void assertChangesWithOldChangeset() throws Exception {
        StringReader reader = new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25 changed and created one\n" +
                "12493     SND\\redsolo_cp 2008-jun-27 13:19:41 changed and created one\n" +
                "12492     SND\\redsolo_cp 2008-jun-27 13:11:15 first file\n" +
                "12472     RNO\\_MCLWEB    2008-jun-20 11:16:06 Created team project folder $/tfsandbox via the Team Project Creation Wizard\n");
        
        BriefHistoryCommand command = new BriefHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 06, 21), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 3, list.size());
        assertEquals("Version was incorrect", "12495", list.get(0).getVersion());
        assertEquals("User was incorrect", "redsolo_cp", list.get(0).getUser());
        assertEquals("Domain was incorrect", "SND", list.get(0).getDomain());
        assertEquals("Comment was incorrect", "changed and created one", list.get(0).getComment());
    }
    
    @Test
    public void assertPollChangesWithUsLocaleOutput() throws Exception {
        Locale.setDefault(Locale.US);
        StringReader reader = new StringReader(
                "Changeset User         Date                 Comment\n" +
                "--------- ------------ ----------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "59977     TLR\\U0000000 Jul 9, 2008 10:23:46 AM Test for Hudson TFS\n");
        
        BriefHistoryCommand command = new BriefHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 06, 01), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 1, list.size());
        assertEquals("Version was incorrect", "59977", list.get(0).getVersion());
        assertEquals("User was incorrect", "U0000000", list.get(0).getUser());
        assertEquals("Domain was incorrect", "TLR", list.get(0).getDomain());
        assertEquals("Comment was incorrect", "Test for Hudson TFS", list.get(0).getComment());
    }
}
