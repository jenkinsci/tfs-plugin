package hudson.plugins.tfs.model;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import hudson.plugins.tfs.util.MaskedArgumentListBuilder;

import java.io.StringReader;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoAnnotations.Mock;


public class WorkspaceTest {

    @Mock private Server server; 
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void assertMapWorkfolderIsExecuted() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));        
        Workspace workspace = new Workspace(server, "name");
        workspace.mapWorkfolder(new Project(server, "$/serverpath"), ".");        
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }
    
    @Test
    public void assertUnmapWorkfolderIsExecuted() throws Exception {
        stub(server.execute(isA(MaskedArgumentListBuilder.class))).toReturn(new StringReader(""));        
        Workspace workspace = new Workspace(server, "name");
        workspace.unmapWorkfolder(".");        
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }
    
    @Test
    public void assertExistsUsesWorkspacesClass() throws Exception {
        Workspaces workspaces = mock(Workspaces.class);
        stub(server.getWorkspaces()).toReturn(workspaces);
        stub(workspaces.exists(isA(Workspace.class))).toReturn(Boolean.TRUE);
        
        Workspace workspace = new Workspace(server, "name");
        assertTrue("The workspace is not reported as existing", workspace.exists());
        
        verify(workspaces).exists(new Workspace(server, "name"));
    }
}
