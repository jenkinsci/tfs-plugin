package hudson.plugins.tfs.action;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.StringReader;
import java.util.Calendar;

import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.model.TeamFoundationProject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;

public class DefaultPollActionTest {

    private TeamFoundationProject project;
    @Mock TfTool tool;

    @Before
    public void setup() throws Exception {
        project = new TeamFoundationProject("server", "path");
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void assertNoChangesWithEmptyOutput() throws Exception {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader(""));
        
        DefaultPollAction action = new DefaultPollAction();
        boolean hasChanges = action.hasChanges(tool, project, Calendar.getInstance());
        assertFalse("Changes were reported when there were none", hasChanges);
    }
    
    @Test
    public void assertChangesWithEmptyToolOutput() throws Exception {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader("No history entries were found for the item and version combination specified.\n\n"));
        
        DefaultPollAction action = new DefaultPollAction();
        boolean hasChanges = action.hasChanges(tool, project, Calendar.getInstance());
        assertFalse("Changes were reported when there were none", hasChanges);
    }    
    
    @Test
    public void assertChangesWithChangeOutput() throws Exception {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader(
                "Changeset User           Date                 Comment\n" +
                "--------- -------------- -------------------- ----------------------------------------------------------------------------\n" +
                "\n" +
                "12495     SND\\redsolo_cp 2008-jun-27 13:21:25 changed and created one\n" +
                "12493     SND\\redsolo_cp 2008-jun-27 13:19:41 changed and created one\n" +
                "12492     SND\\redsolo_cp 2008-jun-27 13:11:15 first file\n" +
                "12472     RNO\\_MCLWEB    2008-jun-27 11:16:06 Created team project folder $/tfsandbox via the Team Project Creation Wizard\n"));
        
        DefaultPollAction action = new DefaultPollAction();
        boolean hasChanges = action.hasChanges(tool, project, Calendar.getInstance());
        assertTrue("Changes were not reported when there were some", hasChanges);
    }
}
