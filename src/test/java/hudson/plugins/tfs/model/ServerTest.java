package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import hudson.plugins.tfs.TfTool;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        assertNotNull("Project object can not be null", server.getProject("$/projectPath", new ArrayList<String>()));
        assertSame("getProject() returned different objects", 
                server.getProject("$/projectPath", new ArrayList<String>()), server.getProject("$/projectPath", new ArrayList<String>()));
    }
    
    @Test
    public void assertGetProjectWithDifferentProjectPathReturnsNotSameInstance() {
        Server server = new Server("url");
        assertNotSame("getProject() did not return different objects", 
                server.getProject("$/projectPath", new ArrayList<String>()), server.getProject("$/otherPath", new ArrayList<String>()));
    }
    
    @Test
    public void assertLocalHostnameIsRetrievedFromTfTool() throws Exception {
        when(tool.getHostname()).thenReturn("thehostname");
        Server server = new Server(tool, "url", null, null);
        try {
            assertEquals("Hostname was incorrect", "thehostname", server.getLocalHostname());
            verify(tool).getHostname();
        } finally {
            server.close();
        }
    }
}
