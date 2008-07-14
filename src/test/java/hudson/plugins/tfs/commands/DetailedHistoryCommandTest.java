package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.commands.DetailedHistoryCommand;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.ChangeSet.Item;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.plugins.tfs.SwedishLocaleTestCase;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

public class DetailedHistoryCommandTest extends SwedishLocaleTestCase {

    @Test
    public void assertBriefHistoryArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        stub(config.getUrl()).toReturn("https//tfs02.codeplex.com");
        stub(config.getUserName()).toReturn("snd\\user_cp");
        stub(config.getUserPassword()).toReturn("password");
        
        Calendar fromTimestamp = Util.getCalendar(2006, 12, 01, 01, 01, 01);
        Calendar toTimestamp = Util.getCalendar(2008, 06, 27, 20, 00, 0);
        
        MaskedArgumentListBuilder arguments = new DetailedHistoryCommand(config, "$/tfsandbox", fromTimestamp, toTimestamp).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("history $/tfsandbox /noprompt /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:detailed /server:https//tfs02.codeplex.com /login:snd\\user_cp,password", arguments.toStringWithQuote());
    }
    
    @Test
    public void assertParsingOfEmptyReader() throws Exception {
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        List<ChangeSet> list = command.parse(new StringReader(""));
        assertNotNull("The list of change sets was null", list);
        assertTrue("The list of change sets was not empty", list.isEmpty());   
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        StringReader reader = new StringReader("No history entries were found for the item and version combination specified.\n\n");
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);
        assertNotNull("The list of change sets was null", list);
        assertTrue("The list of change sets was not empty", list.isEmpty());
    }
    
    @Test
    public void assertOneChangeSetFromFile() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("tf-changeset-1.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);

        assertNotNull("The list of change sets was null", list);
        assertEquals("The number of change sets in the list was incorrect", 1, list.size());
        
        ChangeSet changeSet = list.get(0);
        assertEquals("The versionwas incorrect", "12472", changeSet.getVersion());
        assertEquals("The user was incorrect", "_MCLWEB", changeSet.getUser());
        assertEquals("The user was incorrect", "RNO", changeSet.getDomain());
        //assertEquals("The date was incorrect", TestUtil.getCalendar(2008, 06, 27, 11, 16, 06).getTime(), changeSet.getDate());
        assertEquals("The comment was incorrect", "Created team project folder $/tfsandbox via the Team Project Creation Wizard", changeSet.getComment());
        
        Item item = changeSet.getItems().get(0);
        assertEquals("The item path was incorrect", "$/tfsandbox", item.getPath());
        assertEquals("The item action was incorrect", "add", item.getAction());
    }
    
    @Test
    public void assertTwoChangeSetFromFile() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("tf-changeset-2.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);

        assertNotNull("The list of change sets was null", list);
        assertEquals("The number of change sets in the list was incorrect", 2, list.size());
        
        ChangeSet changeSet = list.get(0);
        assertEquals("The version was incorrect", "12472", changeSet.getVersion());
        assertEquals("The user was incorrect", "_MCLWEB", changeSet.getUser());
        assertEquals("The user was incorrect", "RNO", changeSet.getDomain());
        //assertEquals("The date was incorrect", TestUtil.getCalendar(2008, 06, 27, 11, 16, 06).getTime(), changeSet.getDate());
        assertEquals("The comment was incorrect", "Created team project folder $/tfsandbox via the Team Project Creation Wizard", changeSet.getComment());

        changeSet = list.get(1);
        assertEquals("The version was incorrect", "12492", changeSet.getVersion());
        assertEquals("The user was incorrect", "redsolo_cp", changeSet.getUser());
        assertEquals("The user was incorrect", "SND", changeSet.getDomain());
        //assertEquals("The date was incorrect", TestUtil.getCalendar(2008, 06, 27, 13, 19, 49).getTime(), changeSet.getDate());
        assertEquals("The comment was incorrect", "first file", changeSet.getComment());
    }
    
    @Test
    public void assertTwoItemsInAChangeSet() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("tf-changeset-3.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);
        
        assertNotNull("The list of change sets was null", list);
        assertEquals("The number of change sets in the list was incorrect", 4, list.size());
        
        List<Item> items = list.get(3).getItems();
        assertEquals("Number of items in change set was incorrect", 2, items.size());
    }
    
    @Test(expected=ParseException.class)
    public void assertParseExceptionWhenParsingInvalidDate() throws Exception {
        StringReader reader = new StringReader(
                "-----------------------------------------\n" +
                "Changeset: 12492\n" +
                "User:      SND\\redsolo_cp\n" +
                "Date:      this is no date\n" +
                "\n" +
                "Comment:\n" +
                "  first file");
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        command.parse(reader);
    }

    @Test
    public void assertOldChangeSetAreIgnored() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("tf-changeset-2.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 06, 15), Calendar.getInstance());
        List<ChangeSet> list = command.parse(reader);
        assertNotNull("The list of change sets was null", list);
        assertEquals("The number of change sets in the list was incorrect", 1, list.size());
    }
    
    @Test(expected=ParseException.class)
    public void assertParseExceptionWhenParsingBadFilePath() throws Exception {
        StringReader reader = new StringReader(
                "-----------------------------------------\n" +
                "Changeset: 12492\n" +
                "User:      SND\\redsolo_cp\n" +
                "Date:      2008-jun-27 11:16:06\n" +
                "\n" +
                "Comment:\n" +
                "    Created team project folder $/tfsandbox via the Team Project Creation Wizard\n" +
                "\n" +
                "Items:\n" +
                "    add tfsandbox\n");
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        command.parse(reader);
    }
}
