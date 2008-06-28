package hudson.plugins.tfs.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hudson.plugins.tfs.TestUtil;
import hudson.plugins.tfs.model.TeamFoundationCredentials;
import hudson.plugins.tfs.model.TeamFoundationProject;
import hudson.util.ArgumentListBuilder;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;


public class ToolArgumentBuilderTest {
    
    private ToolArgumentBuilder builder;
    private TeamFoundationProject project;

    @Before
    public void setup() {
        project = new TeamFoundationProject("https://tfs02.codeplex.com", "$/tfsandbox");
        builder = new ToolArgumentBuilder(project);
    }
    
    @Test
    public void assertBriefHistoryCommands() {
        Calendar fromTimestamp = TestUtil.getCalendar(2006, 11, 01, 01, 01, 01);
        Calendar toTimestamp = TestUtil.getCalendar(2008, 05, 27, 20, 00, 0);
        
        ArgumentListBuilder commands = builder.getBriefHistoryArguments(fromTimestamp, toTimestamp);
        assertNotNull("Arguments were null", commands);
        assertEquals("history /noprompt /server:https://tfs02.codeplex.com $/tfsandbox /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:brief", commands.toStringWithQuote());
    }
    
    @Test
    public void assertBriefHistoryCommandsWithCredentials() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        
        Calendar fromTimestamp = TestUtil.getCalendar(2006, 11, 01,01, 01, 01);
        Calendar toTimestamp = TestUtil.getCalendar(2008, 05, 27, 20, 00, 0);
        
        ArgumentListBuilder commands = builder.getBriefHistoryArguments(fromTimestamp, toTimestamp);
        assertNotNull("Arguments were null", commands);
        assertEquals("history /noprompt /server:https://tfs02.codeplex.com $/tfsandbox /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:brief /login:user@domain,password", commands.toStringWithQuote());
    }

    @Test
    public void assertDetailedHistoryCommands() {
        Calendar fromTimestamp = TestUtil.getCalendar(2006, 11, 01, 01, 01, 01);
        Calendar toTimestamp = TestUtil.getCalendar(2008, 05, 27, 20, 00, 0);
        
        ArgumentListBuilder commands = builder.getDetailedHistoryArguments(fromTimestamp, toTimestamp);
        assertNotNull("Arguments were null", commands);
        assertEquals("history /noprompt /server:https://tfs02.codeplex.com $/tfsandbox /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:detailed", commands.toStringWithQuote());
    }
}
