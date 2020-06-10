package hudson.plugins.tfs.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import hudson.plugins.tfs.commands.ListWorkspacesCommand;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import hudson.remoting.Callable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class WorkspacesTest {

    private static final List<String> EMPTY_CLOAKED_PATHS_LIST = Collections.emptyList();
    private static final Map<String, String> EMPTY_MAPPED_PATHS_MAP = new TreeMap<String, String>();

    @Mock private Server server;
    private ListWorkspacesCommand parser;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        parser = new ListWorkspacesCommand(server);
    }

    private List<Workspace> parse(final String s) throws IOException {
        final Reader reader = new StringReader(s);
        return parser.parse(reader);
    }

    @Test
    public void assertListFromServerIsParsedProperly() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(parse(
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "name1     SND\\redsolo_cp COMPUTER\n"));
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.getWorkspace("name1");
        assertNotNull("Workspace was null", workspace);
    }
    
    @Test
    public void assertListFromServerIsRetrievedOnce() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(parse(
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "name1     SND\\redsolo_cp COMPUTER\n"));
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.getWorkspace("name1");
        assertNotNull("Workspace was null", workspace);
        workspace = workspaces.getWorkspace("name1");
        assertNotNull("Workspace was null", workspace);
        
        verify(server, times(1)).execute(isA(Callable.class));
    }

    @Test
    public void assertExistsWorkspace() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(parse(
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "name1     SND\\redsolo_cp COMPUTER\n"));
        
        Workspaces workspaces = new Workspaces(server);
        assertTrue("The workspace was reported as non existant", workspaces.exists(new Workspace("name1")));
    }

    @Test
    public void assertWorkspaceExistsWithOnlyName() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(parse(
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "name1     SND\\redsolo_cp COMPUTER\n"));
        
        Workspaces workspaces = new Workspaces(server);
        assertTrue("The workspace was reported as non existant", workspaces.exists("name1"));
    }

    @Test
    public void assertNewWorkspaceIsAddedToMap() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(null);
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.newWorkspace("name1", null, EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, null);
        assertNotNull("The new workspace was null", workspace);
        assertTrue("The workspace was reported as non existant", workspaces.exists(workspace));
    }

    @Test
    public void assertGettingNewWorkspaceIsNotRetrievingServerList() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(null);
        
        Workspaces workspaces = new Workspaces(server);
        workspaces.newWorkspace("name1", null, EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, null);
        assertNotNull("The get new workspace returned null", workspaces.getWorkspace("name1"));
        verify(server, times(1)).execute(isA(Callable.class));
    }

    @Test
    public void assertNewWorkspaceExistsIsNotRetrievingServerList() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(null);
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.newWorkspace("name1", null, EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, null);
        assertTrue("The get new workspace did not exists", workspaces.exists(workspace));
        verify(server, times(1)).execute(isA(Callable.class));
    }

    @Test
    public void assertWorkspaceIsDeletedFromMap() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(parse(""));
        Workspaces workspaces = new Workspaces(server);
        // Populate the map in test object
        assertFalse("The workspace was reported as existant", workspaces.exists(new Workspace("name")));
        Workspace workspace = workspaces.newWorkspace("name", null, EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, null);
        assertTrue("The workspace was reported as non existant", workspaces.exists(new Workspace("name")));
        workspaces.deleteWorkspace(workspace);
        assertFalse("The workspace was reported as existant", workspaces.exists(workspace));
    }
    
    @Test
    public void assertGetUnknownWorkspaceReturnsNull() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(parse(""));
        Workspaces workspaces = new Workspaces(server);
        assertNull("The unknown workspace was not null", workspaces.getWorkspace("name1"));
    }
    
    @Test
    public void assertUnknownWorkspaceDoesNotExists() throws Exception {
        when(server.execute(isA(Callable.class))).thenReturn(parse(""));
        Workspaces workspaces = new Workspaces(server);
        assertFalse("The unknown workspace was reported as existing", workspaces.exists(new Workspace("name1")));
    }
    
    @Test
    public void assertWorkspaceFactory() {        
        ListWorkspacesCommand.WorkspaceFactory factory = new Workspaces(server);
        Workspace workspace = factory.createWorkspace("name", "computer", "owner", "comment");
        assertEquals("Workspace name was incorrect", "name", workspace.getName());
        assertEquals("Workspace comment was incorrect", "comment", workspace.getComment());
    }
}
