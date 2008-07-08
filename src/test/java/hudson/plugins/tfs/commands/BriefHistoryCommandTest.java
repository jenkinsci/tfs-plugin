package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import hudson.plugins.tfs.SwedishLocaleTestCase;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.commands.BriefHistoryCommand;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.StringReader;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

public class BriefHistoryCommandTest extends SwedishLocaleTestCase {
    
    @Test
    public void assertBriefHistoryArguments() {
        Calendar fromTimestamp = Util.getCalendar(2006, 12, 01, 01, 01, 01);
        Calendar toTimestamp = Util.getCalendar(2008, 06, 27, 20, 00, 0);
        
        MaskedArgumentListBuilder arguments = new BriefHistoryCommand("$/tfsandbox", fromTimestamp, toTimestamp).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("history $/tfsandbox /noprompt /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:brief", arguments.toStringWithQuote());
    }

    @Test
    public void assertNoChangesWithEmptyOutput() throws Exception {
        BriefHistoryCommand command = new BriefHistoryCommand("$/tfsandbox", Calendar.getInstance(), null);
        List<ChangeSet> list = command.parse(new StringReader(""));
        assertEquals("Number of change sets was incorrect", 0, list.size());
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        BriefHistoryCommand command = new BriefHistoryCommand("$/tfsandbox", Calendar.getInstance(), null);
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
        
        BriefHistoryCommand command = new BriefHistoryCommand("$/tfsandbox", Util.getCalendar(2008, 06, 01), null);
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 4, list.size());
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
        
        BriefHistoryCommand command = new BriefHistoryCommand("$/tfsandbox", Util.getCalendar(2008, 06, 21), null);
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 3, list.size());
    }
}
