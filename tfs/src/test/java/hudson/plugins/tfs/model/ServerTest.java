package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

public class ServerTest {

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    static Server createServer() throws IOException {
        return new Server(null, null, "url", null, null, null, ExtraSettings.DEFAULT);
    }

    @Test
    public void assertGetWorkspacesReturnSameObject() throws IOException {
        Server server = createServer();
        assertNotNull("Workspaces object can not be null", server.getWorkspaces());
        assertSame("getWorkspaces() returned different objects", server.getWorkspaces(), server.getWorkspaces());
    }

    @Test
    public void assertGetProjectWithSameProjectPathReturnsSameInstance() throws IOException {
        Server server = createServer();
        assertNotNull("Project object can not be null", server.getProject("$/projectPath"));
        assertSame("getProject() returned different objects", 
                server.getProject("$/projectPath"), server.getProject("$/projectPath"));
    }
    
    @Test
    public void assertGetProjectWithDifferentProjectPathReturnsNotSameInstance() throws IOException {
        Server server = createServer();
        assertNotSame("getProject() did not return different objects", 
                server.getProject("$/projectPath"), server.getProject("$/otherPath"));
    }
}
