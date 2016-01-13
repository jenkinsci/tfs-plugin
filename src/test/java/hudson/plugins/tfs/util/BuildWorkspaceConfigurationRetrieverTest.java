package hudson.plugins.tfs.util;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;

import hudson.model.AbstractBuild;
import hudson.model.Node;
import hudson.model.Run;
import hudson.plugins.tfs.model.WorkspaceConfiguration;
import hudson.plugins.tfs.util.BuildWorkspaceConfigurationRetriever.BuildWorkspaceConfiguration;

import org.junit.Test;
import org.jvnet.hudson.test.Bug;

@SuppressWarnings("unchecked")
public class BuildWorkspaceConfigurationRetrieverTest {
    @Test
    public void assertGetLatestConfgiurationOnNode() {
        AbstractBuild build = mock(AbstractBuild.class);
        Node node = mock(Node.class);
        Node needleNode = mock(Node.class);
        WorkspaceConfiguration configuration = new WorkspaceConfiguration("serverUrl", "workspaceName", "projectPath", new ArrayList<String>(), "workfolder");
        when(build.getPreviousBuild()).thenReturn(build).thenReturn(null);
        when(build.getBuiltOn()).thenReturn(node, node, null);
        when(node.getNodeName()).thenReturn("node1", "needleNode");
        when(needleNode.getNodeName()).thenReturn("needleNode", "needleNode");
        when(build.getAction(WorkspaceConfiguration.class)).thenReturn(configuration);
        
        assertThat( new BuildWorkspaceConfigurationRetriever().getLatestForNode(needleNode, build), equalTo(configuration));
        
        verify(build, times(2)).getBuiltOn();
        verify(node, times(2)).getNodeName();
        verify(build).getPreviousBuild();
        verify(build).getAction(WorkspaceConfiguration.class);
        verifyNoMoreInteractions(node);
        verifyNoMoreInteractions(build);  
    }
    
    @Test
    public void assertGetLatestConfgiurationOnNodeWithNoPrevioudBuild() {
        AbstractBuild build = mock(AbstractBuild.class);
        Node node = mock(Node.class);
        Node needleNode = mock(Node.class);
        when(build.getPreviousBuild()).thenReturn(null);
        when(build.getBuiltOn()).thenReturn(node);
        when(node.getNodeName()).thenReturn("node1");
        when(needleNode.getNodeName()).thenReturn("needleNode");
        
        BuildWorkspaceConfigurationRetriever retriever = new BuildWorkspaceConfigurationRetriever();
        assertThat( retriever.getLatestForNode(needleNode, build), nullValue());
        
        verify(build).getBuiltOn();
        verify(node).getNodeName();
        verify(build).getPreviousBuild();
        verifyNoMoreInteractions(node);
        verifyNoMoreInteractions(build);  
    }
    
    @Test
    public void assertGetLatestConfigurationOnNodeWithNoPrevioudScmConfiguration() {
        AbstractBuild build = mock(AbstractBuild.class);
        Node node = mock(Node.class);
        Node needleNode = mock(Node.class);
        when(build.getBuiltOn()).thenReturn(node);
        when(node.getNodeName()).thenReturn("needleNode");
        when(needleNode.getNodeName()).thenReturn("needleNode");
        
        assertThat( new BuildWorkspaceConfigurationRetriever().getLatestForNode(needleNode, build), nullValue());
        
        verify(build).getBuiltOn();
        verify(node).getNodeName();
        verify(build).getAction(WorkspaceConfiguration.class);
        verifyNoMoreInteractions(node);
        verifyNoMoreInteractions(build);  
    }
    
    @Test
    public void assertGetLatestConfgiurationOnNodeWithNoAbstractBuild() {
        Run run = mock(Run.class);
        Node node = mock(Node.class);
        assertThat( new BuildWorkspaceConfigurationRetriever().getLatestForNode(node, run), nullValue());
        verifyNoMoreInteractions(run);
        verifyNoMoreInteractions(node);
    }

    @Test
    public void assertSaveWorkspaceConfigurationUsesSaveOnBuild() throws IOException {
        AbstractBuild build = mock(AbstractBuild.class);
        Node node = mock(Node.class);
        when(build.getPreviousBuild()).thenReturn(build);
        when(build.getBuiltOn()).thenReturn(node);
        when(node.getNodeName()).thenReturn("needleNode");
        when(build.getAction(WorkspaceConfiguration.class)).thenReturn(new WorkspaceConfiguration("serverUrl", "workspaceName", "projectPath", new ArrayList<String>(), "workfolder"));
        
        BuildWorkspaceConfiguration configuration = new BuildWorkspaceConfigurationRetriever().getLatestForNode(node, build);
        assertThat( configuration.getWorkspaceName(), is("workspaceName"));
        configuration.save();
        
        verify(build).save();  
    }
    
    @Bug(8322)
    @Test
    public void assertGetLatestConfgiurationOnPreviousDeletedNode() {
        AbstractBuild build = mock(AbstractBuild.class);
        Node node = mock(Node.class);
        Node needleNode = mock(Node.class);
        WorkspaceConfiguration configuration = new WorkspaceConfiguration("serverUrl", "workspaceName", "projectPath", new ArrayList<String>(), "workfolder");
        when(build.getPreviousBuild()).thenReturn(build).thenReturn(null);
        when(build.getBuiltOn()).thenReturn(null);


        assertThat( new BuildWorkspaceConfigurationRetriever().getLatestForNode(needleNode, build), nullValue());  
    }
 }
