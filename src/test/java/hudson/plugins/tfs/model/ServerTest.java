package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;

public class ServerTest {

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void assertGetWorkspacesReturnSameObject() throws IOException {
        Server server = new Server("url");
        assertNotNull("Workspaces object can not be null", server.getWorkspaces());
        assertSame("getWorkspaces() returned different objects", server.getWorkspaces(), server.getWorkspaces());
    }
    
    @Test
    public void assertGetProjectWithSameProjectPathReturnsSameInstance() throws IOException {
        Server server = new Server("url");
        assertNotNull("Project object can not be null", server.getProject("$/projectPath", new ArrayList<String>()));
        assertSame("getProject() returned different objects", 
                server.getProject("$/projectPath", new ArrayList<String>()), server.getProject("$/projectPath", new ArrayList<String>()));
    }
    
    @Test
    public void assertGetProjectWithDifferentProjectPathReturnsNotSameInstance() throws IOException {
        Server server = new Server("url");
        assertNotSame("getProject() did not return different objects", 
                server.getProject("$/projectPath", new ArrayList<String>()), server.getProject("$/otherPath", new ArrayList<String>()));
    }
}
