package hudson.plugins.tfs.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;

import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("unchecked")
public class BuildVariableResolverTest {

    @Mock private AbstractProject<?, ?> project;
    @Mock private Computer computer;
    @Mock private AbstractBuild build;

    @Before public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void assertConstructorBuildUsesProject() throws IOException, InterruptedException {
        when(build.getProject()).thenReturn(project);
        new BuildVariableResolver(build, computer);
        verify(build).getProject();
        verifyZeroInteractions(project);
        verifyZeroInteractions(computer);
    }
    
    @Test public void assertJobNameIsResolved() {
        when(project.getName()).thenReturn("ThisIsAJob");

        BuildVariableResolver resolver = new BuildVariableResolver(project, computer);
        assertEquals("Variable resolution was incorrect", "ThisIsAJob", resolver.resolve("JOB_NAME"));
        verifyZeroInteractions(computer);
    }
    
    @Test public void assertJobNameWithoutComputerIsResolved() {
        when(project.getName()).thenReturn("ThisIsAJob");

        BuildVariableResolver resolver = new BuildVariableResolver(project);
        assertEquals("Variable resolution was incorrect", "ThisIsAJob", resolver.resolve("JOB_NAME"));
        assertNull("Variable resolution was performed", resolver.resolve("NONE_EXISTING_KEY"));
    }
    
    @Test public void assertComputerEnvVarIsResolved() throws Exception {
        EnvVars map = new EnvVars();
        map.put("ENV_VAR", "This is an env var");
        
        when(computer.getEnvironment()).thenReturn(map);

        BuildVariableResolver resolver = new BuildVariableResolver(project, computer);
        assertEquals("Variable resolution was incorrect", "This is an env var", resolver.resolve("ENV_VAR"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertComputerUserNameIsResolved() throws Exception {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("user.name", "Other_user");
        
        when(computer.getSystemProperties()).thenReturn(map);

        BuildVariableResolver resolver = new BuildVariableResolver(project, computer);
        assertEquals("Variable resolution was incorrect", "Other_user", resolver.resolve("USER_NAME"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertNodeNameIsResolved() {
        when(computer.getName()).thenReturn("AKIRA");
        
        BuildVariableResolver resolver = new BuildVariableResolver(project, computer);
        assertEquals("Variable resolution was incorrect", "AKIRA", resolver.resolve("NODE_NAME"));
        verifyZeroInteractions(project);
    }
    
    /**
     * Asserts that NODE_NAME works on the master computer, as the MasterComputer.getName() returns null.
     */
    @Test public void assertMasterNodeNameIsResolved() {
        when(computer.getName()).thenReturn("");
        
        BuildVariableResolver resolver = new BuildVariableResolver(project, computer);
        assertEquals("Variable resolution was incorrect", "MASTER", resolver.resolve("NODE_NAME"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertNoComputeraDoesNotThrowNPEWhenResolvingNodeName() {
        BuildVariableResolver resolver = new BuildVariableResolver(project);
        assertNull("Variable resolution was incorrect", resolver.resolve("NODE_NAME"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertBuildEnvVarIsResolved() throws Exception {
        EnvVars map = new EnvVars();
        map.put("BUILD_ID", "121212");

        when(build.getProject()).thenReturn(project);
        when(build.getEnvironment(TaskListener.NULL)).thenReturn(map);

        BuildVariableResolver resolver = new BuildVariableResolver(build, computer);
        assertEquals("Variable resolution was incorrect", "121212", resolver.resolve("BUILD_ID"));
        verify(build).getEnvironment(TaskListener.NULL);
        verifyZeroInteractions(project);
    }
}
