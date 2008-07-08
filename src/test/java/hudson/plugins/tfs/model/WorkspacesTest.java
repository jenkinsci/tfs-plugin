package hudson.plugins.tfs.model;

import java.io.StringReader;

import hudson.plugins.tfs.commands.ListWorkspacesCommand;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;


public class WorkspacesTest {

    @Mock private Server server; 
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void assertListFromServerIsParsedProperly() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "name1  SND\\redsolo_cp COMPUTER\n"));
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.getWorkspace("name1");
        assertNotNull("Workspace was null", workspace);
    }
    
    @Test
    public void assertListFromServerIsRetrievedOnce() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "name1  SND\\redsolo_cp COMPUTER\n"));
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.getWorkspace("name1");
        assertNotNull("Workspace was null", workspace);
        workspace = workspaces.getWorkspace("name1");
        assertNotNull("Workspace was null", workspace);
        
        verify(server, times(1)).execute(isA(MaskedArgumentListBuilder.class));
    }

    @Test
    public void assertExistsWorkspace() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(
                "--------- -------------- -------- ----------------------------------------------------------------------------------------------------------\n" +
                "\n" +
                "name1  SND\\redsolo_cp COMPUTER\n"));
        
        Workspaces workspaces = new Workspaces(server);
        assertTrue("The workspace was reported as non existant", workspaces.exists(new Workspace(server, "name1")));
    }

    @Test
    public void assertNewWorkspaceIsAddedToMap() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.newWorkspace("name1");
        assertNotNull("The new workspace was null", workspace);
        assertTrue("The workspace was reported as non existant", workspaces.exists(workspace));
    }

    @Test
    public void assertGettingNewWorkspaceIsNotRetrievingServerList() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));
        
        Workspaces workspaces = new Workspaces(server);
        workspaces.newWorkspace("name1");
        assertNotNull("The get new workspace returned null", workspaces.getWorkspace("name1"));
        verify(server, times(1)).execute(isA(MaskedArgumentListBuilder.class));
    }

    @Test
    public void assertNewWorkspaceExistsIsNotRetrievingServerList() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));
        
        Workspaces workspaces = new Workspaces(server);
        Workspace workspace = workspaces.newWorkspace("name1");
        assertTrue("The get new workspace did not exists", workspaces.exists(workspace));
        verify(server, times(1)).execute(isA(MaskedArgumentListBuilder.class));
    }

    @Test
    public void assertWorkspaceIsDeletedFromMap() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));        
        Workspaces workspaces = new Workspaces(server);
        // Populate the map in test object
        assertFalse("The workspace was reported as existant", workspaces.exists(new Workspace(server, "name")));
        Workspace workspace = workspaces.newWorkspace("name");
        assertTrue("The workspace was reported as non existant", workspaces.exists(new Workspace(server, "name")));
        workspaces.deleteWorkspace(workspace);
        assertFalse("The workspace was reported as existant", workspaces.exists(workspace));
    }
    
    @Test
    public void assertGetUnknownWorkspaceReturnsNull() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));        
        Workspaces workspaces = new Workspaces(server);
        assertNull("The unknown workspace was not null", workspaces.getWorkspace("name1"));
    }
    
    @Test
    public void assertUnknownWorkspaceDoesNotExists() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));        
        Workspaces workspaces = new Workspaces(server);
        assertFalse("The unknown workspace was reported as existing", workspaces.exists(new Workspace(server, "name1")));
    }
    
    @Test
    public void assertWorkspaceFactory() {        
        ListWorkspacesCommand.WorkspaceFactory factory = new Workspaces(server);
        Workspace workspace = factory.createWorkspace("name", "computer", "owner", "comment");
        assertEquals("Workspace name was incorrect", "name", workspace.getName());
        assertEquals("Workspace comment was incorrect", "comment", workspace.getComment());
    }
}
