package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import hudson.plugins.tfs.TfTool;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;


public class ServerTest {

    @Mock TfTool tool;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void assertGetWorkspacesReturnSameObject() {
        Server server = new Server("url");
        assertNotNull("Workspaces object can not be null", server.getWorkspaces());
        assertSame("getWorkspaces() returned different objects", server.getWorkspaces(), server.getWorkspaces());
    }
    
    @Test
    public void assertGetProjectWithSameProjectPathReturnsSameInstance() {
        Server server = new Server("url");
        assertNotNull("Project object can not be null", server.getProject("$/projectPath"));
        assertSame("getProject() returned different objects", 
                server.getProject("$/projectPath"), server.getProject("$/projectPath"));
    }
    
    @Test
    public void assertGetProjectWithDifferentProjectPathReturnsNotSameInstance() {
        Server server = new Server("url");
        assertNotSame("getProject() did not return different objects", 
                server.getProject("$/projectPath"), server.getProject("$/otherPath"));
    }
}
