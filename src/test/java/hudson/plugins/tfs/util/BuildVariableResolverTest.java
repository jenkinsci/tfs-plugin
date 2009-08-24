package hudson.plugins.tfs.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@SuppressWarnings("unchecked")
public class BuildVariableResolverTest {

    @Mock private AbstractProject<?, ?> project;
    @Mock private Launcher launcher;
    @Mock private AbstractBuild build;

    @Before public void before() throws Exception {
        MockitoAnnotations.initMocks(this);
    }
        
    @Test public void assertConstructorBuildUsesProject() {
        when(build.getProject()).thenReturn(project);
        new BuildVariableResolver(build, launcher);
        verify(build).getProject();
        verifyZeroInteractions(project);
        verifyZeroInteractions(launcher);
    }
    
    @Test public void assertJobNameIsResolved() {
        when(project.getName()).thenReturn("ThisIsAJob");

        BuildVariableResolver resolver = new BuildVariableResolver(project, launcher);
        assertEquals("Variable resolution was incorrect", "ThisIsAJob", resolver.resolve("JOB_NAME"));
        verifyZeroInteractions(launcher);
    }
    
    @Test public void assertJobNameWithoutLauncherIsResolved() {
        when(project.getName()).thenReturn("ThisIsAJob");

        BuildVariableResolver resolver = new BuildVariableResolver(project);
        assertEquals("Variable resolution was incorrect", "ThisIsAJob", resolver.resolve("JOB_NAME"));
        assertNull("Variable resolution was performed", resolver.resolve("NONE_EXISTING_KEY"));
    }
    
    @Test public void assertComputerEnvVarIsResolved() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("ENV_VAR", "This is an env var");
        
        Computer computer = mock(Computer.class);
        when(launcher.getComputer()).thenReturn(computer);
        when(computer.getEnvVars()).thenReturn(map);

        BuildVariableResolver resolver = new BuildVariableResolver(project, launcher);
        assertEquals("Variable resolution was incorrect", "This is an env var", resolver.resolve("ENV_VAR"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertComputerUserNameIsResolved() throws Exception {
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("user.name", "Other_user");
        
        Computer computer = mock(Computer.class);
        when(launcher.getComputer()).thenReturn(computer);
        when(computer.getSystemProperties()).thenReturn(map);

        BuildVariableResolver resolver = new BuildVariableResolver(project, launcher);
        assertEquals("Variable resolution was incorrect", "Other_user", resolver.resolve("USER_NAME"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertNodeNameIsResolved() {
        Computer computer = mock(Computer.class);
        when(launcher.getComputer()).thenReturn(computer);
        when(computer.getName()).thenReturn("AKIRA");
        
        BuildVariableResolver resolver = new BuildVariableResolver(project , launcher);
        assertEquals("Variable resolution was incorrect", "AKIRA", resolver.resolve("NODE_NAME"));
        verifyZeroInteractions(project);
    }
    
    /**
     * Asserts that NODE_NAME works on the master computer, as the MasterComputer.getName() returns null.
     */
    @Test public void assertMasterNodeNameIsResolved() {
        Computer computer = mock(Computer.class);
        when(launcher.getComputer()).thenReturn(computer);
        when(computer.getName()).thenReturn("");
        
        BuildVariableResolver resolver = new BuildVariableResolver(project , launcher);
        assertEquals("Variable resolution was incorrect", "MASTER", resolver.resolve("NODE_NAME"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertNoComputeraDoesNotThrowNPEWhenResolvingNodeName() {
        when(launcher.getComputer()).thenReturn(null);
        
        BuildVariableResolver resolver = new BuildVariableResolver(project , launcher);
        assertNull("Variable resolution was incorrect", resolver.resolve("NODE_NAME"));
        verifyZeroInteractions(project);
    }
    
    @Test public void assertBuildEnvVarIsResolved() throws Exception {
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("BUILD_ID", "121212");

        when(build.getProject()).thenReturn(project);
        when(build.getEnvVars()).thenReturn(map);

        BuildVariableResolver resolver = new BuildVariableResolver(build, launcher);
        assertEquals("Variable resolution was incorrect", "121212", resolver.resolve("BUILD_ID"));
        verify(build).getEnvVars();
        verifyZeroInteractions(project);
    }
}
