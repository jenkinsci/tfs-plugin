package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class WorkspaceTest {

    @Mock private Server server; 
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void assertExistsUsesWorkspacesClass() throws Exception {
        Workspaces workspaces = mock(Workspaces.class);
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists(isA(Workspace.class))).thenReturn(Boolean.TRUE);
        
        Workspace workspace = new Workspace(server, "name");
        assertTrue("The workspace is not reported as existing", workspace.exists());
        
        verify(workspaces).exists(new Workspace(server, "name"));
    }
}
