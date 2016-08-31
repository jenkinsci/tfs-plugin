package hudson.plugins.tfs.actions;

import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class RemoveWorkspaceActionTest {
    @Mock private Server server;        
    @Mock private Workspaces workspaces;
    @Mock private Workspace workspace;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void assertNoSuchWorkspaceNameDoesNothing() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists(anyString())).thenReturn(false);
        
        RemoveWorkspaceAction action = new RemoveWorkspaceAction("workspace");
        assertThat(action.remove(server), is(false));

        verify(server).getWorkspaces();
        verify(workspaces).exists("workspace");
        verifyNoMoreInteractions(workspace);
        verifyNoMoreInteractions(workspaces);
        verifyNoMoreInteractions(server);
    }

    @Test
    public void assertWorkspaceIsDeleted() throws Exception  {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists(anyString())).thenReturn(true);
        when(workspaces.getWorkspace(anyString())).thenReturn(workspace);
        
        RemoveWorkspaceAction action = new RemoveWorkspaceAction("workspace");
        assertThat(action.remove(server), is(true));

        verify(server).getWorkspaces();
        verify(workspaces).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verify(workspaces).deleteWorkspace(workspace);
        verifyNoMoreInteractions(workspace);
        verifyNoMoreInteractions(workspaces);
        verifyNoMoreInteractions(server);
    }
}
