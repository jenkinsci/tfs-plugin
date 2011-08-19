package hudson.plugins.tfs.commands;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;

import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.commands.DetailedHistoryCommand;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.ChangeSet.Item;
import hudson.plugins.tfs.util.DateParser;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;
import hudson.plugins.tfs.SwedishLocaleTestCase;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;

public class DetailedHistoryCommandTest extends SwedishLocaleTestCase {

    @After public void tearDown() {
        System.getProperties().remove(DetailedHistoryCommand.IGNORE_DATE_CHECK_ON_CHANGE_SET);        
    }

    @Bug(6596)
    @Test
    public void assertBriefHistoryArguments() {
        ServerConfigurationProvider config = mock(ServerConfigurationProvider.class);
        when(config.getUrl()).thenReturn("https//tfs02.codeplex.com");
        when(config.getUserName()).thenReturn("snd\\user_cp");
        when(config.getUserPassword()).thenReturn("password");
        
        Calendar fromTimestamp = Util.getCalendar(2006, 12, 01, 01, 01, 01);
        Calendar toTimestamp = Util.getCalendar(2008, 06, 27, 20, 00, 0);
        
        MaskedArgumentListBuilder arguments = new DetailedHistoryCommand(config, "$/tfsandbox", fromTimestamp, toTimestamp).getArguments();
        assertNotNull("Arguments were null", arguments);
        assertEquals("history $/tfsandbox -noprompt -version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:01Z -recursive -format:detailed -server:https//tfs02.codeplex.com -login:snd\\user_cp,password", arguments.toStringWithQuote());
        assertEquals(toTimestamp, Util.getCalendar(2008, 06, 27, 20, 00, 0));
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

    @Test
    public void assertOldChangeSetAreNotIgnoredIfSystemPropertyIsSet() throws Exception {
        System.setProperty(DetailedHistoryCommand.IGNORE_DATE_CHECK_ON_CHANGE_SET,"true");
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("tf-changeset-2.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2009, 06, 15), Util.getCalendar(2009, 06, 16));
        List<ChangeSet> list = command.parse(reader);
        assertNotNull("The list of change sets was null", list);
        assertEquals("The number of change sets in the list was incorrect", 2, list.size());
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

    @Bug(10784)
    @Test
    public void assertCanParseGermanChangesetWithCheckinNotes() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("tf-changeset-german-1.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", 
                Util.getCalendar(2011, 07, 10), Calendar.getInstance(), 
                new DateParser(new Locale("de", "DE"), TimeZone.getTimeZone("Germany")));
        List<ChangeSet> list = command.parse(reader);
        assertNotNull("The list of change sets was null", list);
        assertEquals("The number of change sets in the list was incorrect", 1, list.size());
    }
    
    @Test
    public void assertNoCrashForIssue3683() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("issue-3683.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", 
                Util.getCalendar(2009, 05, 13, 21, 55, 33, TimeZone.getDefault()), 
                Util.getCalendar(2009, 05, 13, 22, 43, 05, TimeZone.getDefault()), 
                new DateParser(new Locale("en", "ml"), TimeZone.getDefault()));
        // Need to use the current locale as the Date.parse() will parse the date
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 3, list.size());
    }

    @Test
    public void assertLongChangesetsCanBeParsed() throws Exception {
        StringBuilder builder = new StringBuilder("-----------------------------------\n" +
                "Changeset: 12472\n" +
                "User:      RNO\\_MCLWEB\n" +
                "Date:      2008-jun-27 11:16:06\n" +
                "\n" +
                "Comment:\n" +
                "Created team project folder $/tfsandbox via the Team Project Creation Wizard\n" +
                "\n" +
                "Items:\n"
        );

        for (int i = 0; i < 40000; i++) {
            builder.append("  add $/tfsandbox/file" + i);
        }

        builder.append("\n\n");

        StringReader stringReader = new StringReader(builder.toString());
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", Util.getCalendar(2008, 01, 15), Calendar.getInstance());
        List<ChangeSet> list = command.parse(stringReader);
        assertEquals("Number of change sets was incorrect", 1, list.size());
    }
    
    /**
     * Asserts that the TF date output can be parsed correctly.
     * It seems that the "p.m." could not be parsed properly, and would yield incorrect values. 
     * The default date formats can only handle PM or AM (no dots).
     * @throws Exception thrown if test error
     */
    @Bug(4184)
    @Test
    public void assertParsingOfDatesReportedInIssue4184Works() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("issue-4184.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), 
                "$/tfsandbox", 
                Util.getCalendar(2009, 8, 10, 5, 11, 2, "GMT"), 
                Util.getCalendar(2009, 8, 10, 5, 19, 0, "GMT"),
                new DateParser(new Locale("en", "nz"), TimeZone.getTimeZone("Pacific/Auckland")));
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 1, list.size());
    }

    @Bug(4943)
    @Test
    public void assertCheckinOnBehalfOfOtherUserWorks() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("issue-4943.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), "$/tfsandbox", 
                Util.getCalendar(2009, 01, 01, 21, 55, 33, TimeZone.getDefault()), 
                Util.getCalendar(2010, 01, 01, 22, 43, 05, TimeZone.getDefault()));
        // Need to use the current locale as the Date.parse() will parse the date
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 2, list.size());
        assertThat(list.get(1).getCheckedInBy(), is("USERB"));
    }

    /**
     * The MS TEE tfs command line tool does not output the line separator in the begining anymore.
     * @throws Exception thrown if test error
     */
    @Bug(6870)
    @Test
    public void assertThatSingleChangesetWithoutSeparatorIsParsedProperly() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("issue-6870.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), 
                "$/tfsandbox", 
                Util.getCalendar(2009, 8, 10, 5, 11, 2, "GMT"), 
                Util.getCalendar(2010, 9, 10, 5, 19, 0, "GMT"), 
                new DateParser(new Locale("de", "DE"), TimeZone.getTimeZone("Germany")));
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 1, list.size());
    }
    @Bug(6870)
    @Test
    public void assertThatMultiChangesetWithoutSeparatorIsParsedProperly() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("issue-6870-2.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), 
                "$/tfsandbox", 
                Util.getCalendar(2009, 8, 10, 5, 11, 2, "GMT"), 
                Util.getCalendar(2010, 9, 10, 5, 19, 0, "GMT"), 
                new DateParser(new Locale("de", "DE"), TimeZone.getTimeZone("Germany")));
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 2, list.size());
    }

    /** HUDSON-6454 was actually caused by HUDSON-6870. */
    @Bug(6454)
    @Test
    public void assertThatMovedKeywordsAreProperlyRead() throws Exception {
        InputStreamReader reader = new InputStreamReader(DetailedHistoryCommandTest.class.getResourceAsStream("issue-6454.txt"));
        DetailedHistoryCommand command = new DetailedHistoryCommand(mock(ServerConfigurationProvider.class), 
                "$/tfsandbox", 
                Util.getCalendar(2009, 8, 10, 5, 11, 2, "GMT"), 
                Util.getCalendar(2010, 9, 10, 5, 19, 0, "GMT"));
        List<ChangeSet> list = command.parse(reader);
        assertEquals("Number of change sets was incorrect", 1, list.size());
    }
}
