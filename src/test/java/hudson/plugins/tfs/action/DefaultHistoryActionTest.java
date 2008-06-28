package hudson.plugins.tfs.action;

import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.model.TeamFoundationChangeSet;
import hudson.plugins.tfs.model.TeamFoundationProject;
import hudson.plugins.tfs.model.TeamFoundationChangeSet.Item;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;

public class DefaultHistoryActionTest {

    private TeamFoundationProject project;
    @Mock TfTool tool;

    @Before
    public void setup() throws Exception {
        project = new TeamFoundationProject("server", "path");
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void assertParsingOfEmptyReader() throws Exception {
        stub(tool.execute(isA(String[].class))).toReturn(new StringReader(""));
        
        DefaultHistoryAction action = new DefaultHistoryAction();
        List<TeamFoundationChangeSet> changeSets = action.getChangeSets(tool, project, Calendar.getInstance(), Calendar.getInstance());
        assertNotNull("The list of change sets was null", changeSets);
        assertTrue("The list of change sets was not empty", changeSets.isEmpty());   
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        stub(tool.execute(isA(String[].class))).toReturn(new StringReader("No history entries were found for the item and version combination specified.\n\n"));
        
        DefaultHistoryAction action = new DefaultHistoryAction();
        List<TeamFoundationChangeSet> changeSets = action.getChangeSets(tool, project, Calendar.getInstance(), Calendar.getInstance());
        assertNotNull("The list of change sets was null", changeSets);
        assertTrue("The list of change sets was not empty", changeSets.isEmpty());
    }
    
    @Test
    public void assertOneChangeSetFromFile() throws Exception {
        stub(tool.execute(isA(String[].class))).toReturn(new InputStreamReader(DefaultHistoryActionTest.class.getResourceAsStream("tf-changeset-1.log")));
        
        DefaultHistoryAction action = new DefaultHistoryAction();
        List<TeamFoundationChangeSet> changeSets = action.getChangeSets(tool, project, Util.getCalendar(2006, 12, 1), Calendar.getInstance());
        assertNotNull("The list of change sets was null", changeSets);
        assertEquals("The number of change sets in the list was incorrect", 1, changeSets.size());
        
        TeamFoundationChangeSet changeSet = changeSets.get(0);
        assertEquals("The versionwas incorrect", "12472", changeSet.getVersion());
        assertEquals("The user was incorrect", "RNO\\_MCLWEB", changeSet.getUser());
        //assertEquals("The date was incorrect", TestUtil.getCalendar(2008, 06, 27, 11, 16, 06).getTime(), changeSet.getDate());
        assertEquals("The comment was incorrect", "Created team project folder $/tfsandbox via the Team Project Creation Wizard", changeSet.getComment());
        
        Item item = changeSet.getItems().get(0);
        assertEquals("The item path was incorrect", "$/tfsandbox", item.getPath());
        assertEquals("The item action was incorrect", "add", item.getAction());
    }
    
    @Test
    public void assertTwoChangeSetFromFile() throws Exception {
        stub(tool.execute(isA(String[].class))).toReturn(new InputStreamReader(DefaultHistoryActionTest.class.getResourceAsStream("tf-changeset-2.log")));
        
        DefaultHistoryAction action = new DefaultHistoryAction();
        List<TeamFoundationChangeSet> changeSets = action.getChangeSets(tool, project, Util.getCalendar(2006, 12, 1), Calendar.getInstance());
        assertNotNull("The list of change sets was null", changeSets);
        assertEquals("The number of change sets in the list was incorrect", 2, changeSets.size());
        
        TeamFoundationChangeSet changeSet = changeSets.get(0);
        assertEquals("The version was incorrect", "12472", changeSet.getVersion());
        assertEquals("The user was incorrect", "RNO\\_MCLWEB", changeSet.getUser());
        //assertEquals("The date was incorrect", TestUtil.getCalendar(2008, 06, 27, 11, 16, 06).getTime(), changeSet.getDate());
        assertEquals("The comment was incorrect", "Created team project folder $/tfsandbox via the Team Project Creation Wizard", changeSet.getComment());

        changeSet = changeSets.get(1);
        assertEquals("The version was incorrect", "12492", changeSet.getVersion());
        assertEquals("The user was incorrect", "SND\\redsolo_cp", changeSet.getUser());
        //assertEquals("The date was incorrect", TestUtil.getCalendar(2008, 06, 27, 13, 19, 49).getTime(), changeSet.getDate());
        assertEquals("The comment was incorrect", "first file", changeSet.getComment());
    }
}
