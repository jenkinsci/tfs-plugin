package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringReader;

import hudson.plugins.tfs.TfTool;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

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
    
    @Test
    public void assertExecuteIsAddingServerArguments() throws IOException, InterruptedException {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader(""));
        Server server = new Server(tool, "https://tfs02.codeplex.com", null, null);
        server.execute((MaskedArgumentListBuilder) new MaskedArgumentListBuilder().add("argument"));
        verify(tool).execute(new String[]{"argument", "/server:https://tfs02.codeplex.com"}, new boolean[]{false, false});
    }
    
    @Test
    public void assertExecuteIsAddingUserCredentials() throws IOException, InterruptedException {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader(""));
        Server server = new Server(tool, "https://tfs02.codeplex.com", "user", "password");
        server.execute((MaskedArgumentListBuilder) new MaskedArgumentListBuilder().add("argument"));
        verify(tool).execute(
                new String[]{"argument", "/server:https://tfs02.codeplex.com", "/login:user,password"}, 
                new boolean[]{false, false, true});
    }
    
    @Test
    public void assertExecuteIsNotAddingInvalidUserCredentials() throws IOException, InterruptedException {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader(""));
        Server server = new Server(tool, "https://tfs02.codeplex.com", "user", null);
        server.execute((MaskedArgumentListBuilder) new MaskedArgumentListBuilder().add("argument"));
        verify(tool).execute(
                new String[]{"argument", "/server:https://tfs02.codeplex.com"}, 
                new boolean[]{false, false});
    }
    
    @Test
    public void assertExecuteIsNotAddingUserCredentialsForEmptyName() throws IOException, InterruptedException {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader(""));
        Server server = new Server(tool, "https://tfs02.codeplex.com", "", "");
        server.execute((MaskedArgumentListBuilder) new MaskedArgumentListBuilder().add("argument"));
        verify(tool).execute(
                new String[]{"argument", "/server:https://tfs02.codeplex.com"}, 
                new boolean[]{false, false});
    }
    
    @Test
    public void assertExecuteIsAddingUserCredentialsForEmptyPassword() throws IOException, InterruptedException {
        stub(tool.execute(isA(String[].class), isA(boolean[].class))).toReturn(new StringReader(""));
        Server server = new Server(tool, "https://tfs02.codeplex.com", "aname", "");
        server.execute((MaskedArgumentListBuilder) new MaskedArgumentListBuilder().add("argument"));
        verify(tool).execute(
                new String[]{"argument", "/server:https://tfs02.codeplex.com", "/login:aname,"}, 
                new boolean[]{false, false, true});
    }
}
