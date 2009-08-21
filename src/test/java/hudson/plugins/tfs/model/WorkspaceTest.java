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
    public void assertMapWorkfolderIsExecuted() throws Exception {
        when(server.execute(isA(MaskedArgumentListBuilder.class))).thenReturn(new StringReader(""));        
        Workspace workspace = new Workspace(server, "name");
        workspace.mapWorkfolder(new Project(server, "$/serverpath"), ".");        
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }
    
    @Test
    public void assertMapWorkfolderClosesReader() throws Exception {
        Reader spy = spy(new StringReader(""));
        when(server.execute(isA(MaskedArgumentListBuilder.class))).thenReturn(spy);        
        new Workspace(server, "name").mapWorkfolder(new Project(server, "$/serverpath"), ".");        
        verify(spy).close();
    }
    
    @Test
    public void assertUnmapWorkfolderIsExecuted() throws Exception {
        when(server.execute(isA(MaskedArgumentListBuilder.class))).thenReturn(new StringReader(""));        
        Workspace workspace = new Workspace(server, "name");
        workspace.unmapWorkfolder(".");        
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }
    
    @Test
    public void assertUnmapWorkfolderClosesReader() throws Exception {
        Reader spy = spy(new StringReader(""));
        when(server.execute(isA(MaskedArgumentListBuilder.class))).thenReturn(spy);        
        new Workspace(server, "name").unmapWorkfolder("$/serverpath");        
        verify(spy).close();
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
    
    @Test
    public void assertGetMappingsIsExecuted() throws Exception {
        when(server.execute(isA(MaskedArgumentListBuilder.class))).thenReturn(new StringReader(""));        
        Workspace workspace = new Workspace(server, "name");
        workspace.getMappings();
        verify(server).execute(isA(MaskedArgumentListBuilder.class));
    }
    
    @Test
    public void assertGetMappingsClosesReader() throws Exception {
        Reader spy = spy(new StringReader(""));
        when(server.execute(isA(MaskedArgumentListBuilder.class))).thenReturn(spy);        
        new Workspace(server, "name").getMappings();        
        verify(spy).close();
    }
}
