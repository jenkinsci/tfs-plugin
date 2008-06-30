package hudson.plugins.tfs.util;

import static org.junit.Assert.*;

import hudson.plugins.tfs.Util;
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
        Calendar fromTimestamp = Util.getCalendar(2006, 12, 01, 01, 01, 01);
        Calendar toTimestamp = Util.getCalendar(2008, 06, 27, 20, 00, 0);
        
        ArgumentListBuilder commands = builder.getBriefHistoryArguments(fromTimestamp, toTimestamp);
        assertNotNull("Arguments were null", commands);
        assertEquals("history /noprompt /server:https://tfs02.codeplex.com $/tfsandbox /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:brief", commands.toStringWithQuote());
    }
    
    @Test
    public void assertBriefHistoryCommandsWithCredentials() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        
        Calendar fromTimestamp = Util.getCalendar(2006, 12, 01,01, 01, 01);
        Calendar toTimestamp = Util.getCalendar(2008, 06, 27, 20, 00, 0);
        
        ArgumentListBuilder commands = builder.getBriefHistoryArguments(fromTimestamp, toTimestamp);
        assertNotNull("Arguments were null", commands);
        assertEquals("history /noprompt /server:https://tfs02.codeplex.com $/tfsandbox /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:brief /login:user@domain,password", commands.toStringWithQuote());
    }
    
    @Test
    public void assertNoneMaskedArgumentsInHistoryCommands() {
        MaskedArgumentListBuilder commands = builder.getBriefHistoryArguments(Calendar.getInstance(), Calendar.getInstance());
        assertEquals("The length of command array was incorrect", 7, commands.toCommandArray().length);
        assertEquals("The length of mask array was incorrect", 7, commands.toMaskArray().length);
        assertArrayEquals("The masked array was incorrect", 
                new Boolean[]{false,false,false,false,false,false,false},
                Util.toBoxedArray(commands.toMaskArray()));
    }
    
    @Test
    public void assertMaskedCredentialsInHistoryCommands() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        MaskedArgumentListBuilder commands = builder.getBriefHistoryArguments(Calendar.getInstance(), Calendar.getInstance());
        assertEquals("The length of command array was incorrect", 8, commands.toCommandArray().length);
        assertEquals("The length of mask array was incorrect", 8, commands.toMaskArray().length);
        assertArrayEquals("The masked array was incorrect", 
                new Boolean[]{false,false,false,false,false,false,false,true},
                Util.toBoxedArray(commands.toMaskArray()));
    }
    
    @Test
    public void assertDetailedHistoryCommands() {
        Calendar fromTimestamp = Util.getCalendar(2006, 12, 01, 01, 01, 01);
        Calendar toTimestamp = Util.getCalendar(2008, 06, 27, 20, 00, 0);
        
        ArgumentListBuilder commands = builder.getDetailedHistoryArguments(fromTimestamp, toTimestamp);
        assertNotNull("Arguments were null", commands);
        assertEquals("history /noprompt /server:https://tfs02.codeplex.com $/tfsandbox /version:D2006-12-01T01:01:01Z~D2008-06-27T20:00:00Z /recursive /format:detailed", commands.toStringWithQuote());
    }

    @Test
    public void assertNewWorkspaceCommands() {
        ArgumentListBuilder commands = builder.getNewWorkspaceArguments("workspacename");
        assertNotNull("Arguments were null", commands);
        assertEquals("workspace /new /server:https://tfs02.codeplex.com workspacename", commands.toStringWithQuote());
    }

    @Test
    public void assertNewWorkspaceCommandsWithCredentials() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));        
        ArgumentListBuilder commands = builder.getNewWorkspaceArguments("workspacename");
        assertNotNull("Arguments were null", commands);
        assertEquals("workspace /new /server:https://tfs02.codeplex.com /login:user@domain,password workspacename", commands.toStringWithQuote());
    }
    
    @Test
    public void assertMaskedCredentialsInNewWorkspaceCommands() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        MaskedArgumentListBuilder commands = builder.getNewWorkspaceArguments("workspacename");
        assertArrayEquals("The masked array was incorrect", 
                new Boolean[]{false,false,false,true,false},
                Util.toBoxedArray(commands.toMaskArray()));
    }

    @Test
    public void assertRemoveWorkspaceCommands() {
        ArgumentListBuilder commands = builder.getDeleteWorkspaceArguments("workspacename");
        assertNotNull("Arguments were null", commands);
        assertEquals("workspace /remove:workspacename /server:https://tfs02.codeplex.com", commands.toStringWithQuote());
    }

    @Test
    public void assertRemoveWorkspaceCommandsWithCredentials() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        ArgumentListBuilder commands = builder.getDeleteWorkspaceArguments("workspacename");
        assertNotNull("Arguments were null", commands);
        assertEquals("workspace /remove:workspacename /server:https://tfs02.codeplex.com /login:user@domain,password", commands.toStringWithQuote());
    }
    
    @Test
    public void assertMaskedCredentialsRemoveNewWorkspaceCommands() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        MaskedArgumentListBuilder commands = builder.getDeleteWorkspaceArguments("workspacename");
        assertArrayEquals("The masked array was incorrect", 
                new Boolean[]{false,false,false,true},
                Util.toBoxedArray(commands.toMaskArray()));
    }

    @Test
    public void assertMapWorkfolderCommands() {
        ArgumentListBuilder commands = builder.getWorkfoldArguments(".", "workspacename");
        assertNotNull("Arguments were null", commands);
        assertEquals("workfold /server:https://tfs02.codeplex.com /workspace:workspacename $/tfsandbox .", commands.toStringWithQuote());
    }

    @Test
    public void assertMapWorkfolderCommandsWithCredentials() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        ArgumentListBuilder commands = builder.getWorkfoldArguments(".", "workspacename");
        assertNotNull("Arguments were null", commands);
        assertEquals("workfold /server:https://tfs02.codeplex.com /login:user@domain,password /workspace:workspacename $/tfsandbox .", commands.toStringWithQuote());
    }
    
    @Test
    public void assertMaskedCredentialsInMapWorkfolderCommands() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        MaskedArgumentListBuilder commands = builder.getWorkfoldArguments(".", "workspacename");
        assertArrayEquals("The masked array was incorrect", 
                new Boolean[]{false,false,true,false,false,false},
                Util.toBoxedArray(commands.toMaskArray()));
    }

    @Test
    public void assertGetFilesCommands() {
        ArgumentListBuilder commands = builder.getGetArguments();
        assertNotNull("Arguments were null", commands);
        assertEquals("get /recursive /server:https://tfs02.codeplex.com", commands.toStringWithQuote());
    }

    @Test
    public void assertGetFilesCommandsWithCredentials() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        ArgumentListBuilder commands = builder.getGetArguments();
        assertNotNull("Arguments were null", commands);
        assertEquals("get /recursive /server:https://tfs02.codeplex.com /login:user@domain,password", commands.toStringWithQuote());
    }

    
    @Test
    public void assertMaskedCredentialsInGetCommands() {
        project.setCredentials(new TeamFoundationCredentials("user", "password", "domain"));
        MaskedArgumentListBuilder commands = builder.getGetArguments();
        assertArrayEquals("The masked array was incorrect", 
                new Boolean[]{false,false,false,true},
                Util.toBoxedArray(commands.toMaskArray()));
    }
}
