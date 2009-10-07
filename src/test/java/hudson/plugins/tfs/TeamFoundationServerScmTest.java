package hudson.plugins.tfs;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.ParametersAction;

import org.junit.After;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class TeamFoundationServerScmTest {

    private FilePath workspace;

    @After public void tearDown() throws Exception {
        if (workspace != null) {
            workspace.deleteRecursive();
            workspace = null;
        }
    }
    
    @Test
    public void assertWorkspaceNameReplacesJobName() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject project = mock(AbstractProject.class);
        when(build.getProject()).thenReturn(project);
        when(project.getName()).thenReturn("ThisIsAJob");
        
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, ".", false, "erik_${JOB_NAME}", "user", "password");
        assertEquals("Workspace name was incorrect", "erik_ThisIsAJob", scm.getWorkspaceName(build, mock(Computer.class)));
    }
    
    @Test 
    public void assertDoUsernameCheckRegexWorks() {
        assertFalse(TeamFoundationServerScm.DescriptorImpl.DOMAIN_SLASH_USER_REGEX.matcher("redsolo").matches());
        assertTrue(TeamFoundationServerScm.DescriptorImpl.DOMAIN_SLASH_USER_REGEX.matcher("snd\\redsolo").matches());
        assertTrue(TeamFoundationServerScm.DescriptorImpl.DOMAIN_SLASH_USER_REGEX.matcher("domain-name.se\\my name").matches());
        assertTrue(TeamFoundationServerScm.DescriptorImpl.DOMAIN_SLASH_USER_REGEX.matcher("domain-NAME.se\\NAME").matches());
        assertFalse(TeamFoundationServerScm.DescriptorImpl.DOMAIN_SLASH_USER_REGEX.matcher("dumb$name\\0349").matches());

        assertFalse(TeamFoundationServerScm.DescriptorImpl.USER_AT_DOMAIN_REGEX.matcher("redsolo").matches());
        assertTrue(TeamFoundationServerScm.DescriptorImpl.USER_AT_DOMAIN_REGEX.matcher("redsolo@snd").matches());
        assertTrue(TeamFoundationServerScm.DescriptorImpl.USER_AT_DOMAIN_REGEX.matcher("my name-andsuch@domain-name.se").matches());
        assertTrue(TeamFoundationServerScm.DescriptorImpl.USER_AT_DOMAIN_REGEX.matcher("DomainNAME@NAME.se").matches());
        assertFalse(TeamFoundationServerScm.DescriptorImpl.USER_AT_DOMAIN_REGEX.matcher("0349@dumb$name").matches());
    }
    
    @Test 
    public void assertDoProjectPathCheckRegexWorks() {
        assertFalse("Project path regex matched an invalid project path", TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX.matcher("tfsandbox").matches());
        assertFalse("Project path regex matched an invalid project path", TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX.matcher("tfsandbox/with/sub/pathes").matches());
        assertFalse("Project path regex matched an invalid project path", TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX.matcher("tfsandbox$/with/sub/pathes").matches());
        assertTrue("Project path regex did not match a valid project path", TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX.matcher("$/tfsandbox").matches());
        assertTrue("Project path regex did not match a valid project path", TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX.matcher("$/tfsandbox/path with space/subpath").matches());
    }
    
    @Test 
    public void assertDoWorkspaceNameCheckRegexWorks() {
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("work space ").matches());
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("work.space.").matches());
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("work*space").matches());
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("work/space").matches());
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("work\"space").matches());
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("workspace*").matches());
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("workspace/").matches());
        assertFalse("Workspace name regex matched an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("workspace\"").matches());
        assertTrue("Workspace name regex dit not match an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("work.space").matches());
        assertTrue("Workspace name regex dit not match an invalid workspace name", TeamFoundationServerScm.DescriptorImpl.WORKSPACE_NAME_REGEX.matcher("work space").matches());
    }
    
    @Test
    public void assertDefaultValueIsUsedForEmptyLocalPath() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "", false, "workspace", "user", "password");
        assertEquals("Default value for work folder was incorrect", ".", scm.getLocalPath());
    }
    
    @Test
    public void assertDefaultValueIsUsedForEmptyWorkspaceName() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", ".", false, "", "user", "password");
        assertEquals("Default value for workspace was incorrect", "Hudson-${JOB_NAME}-${NODE_NAME}", scm.getWorkspaceName());
    }
    
    @Test
    public void assertGetModuleRootReturnsWorkFolder() throws Exception {
        workspace = Util.createTempFilePath();
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "workfolder", false, "", "user", "password");
        FilePath moduleRoot = scm.getModuleRoot(workspace);
        assertEquals("Name for module root was incorrect", "workfolder", moduleRoot.getName());
        assertEquals("The parent for module root was incorrect", workspace.getName(), moduleRoot.getParent().getName());
    }
    
    @Test
    public void assertGetModuleRootWorksForDotWorkFolder() throws Exception {
        workspace = Util.createTempFilePath();
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", ".", false, "", "user", "password");
        FilePath moduleRoot = scm.getModuleRoot(workspace);
        assertTrue("The module root was reported as not existing even if its virtually the same as workspace",
                moduleRoot.exists());
        assertEquals("The module root was not the same as workspace", moduleRoot.lastModified(), workspace.lastModified());
    }
    
    @Test
    public void assertWorkspaceNameIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", ".", false, "WORKSPACE_SAMPLE", "user", "password");
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject project = mock(AbstractProject.class);
        when(build.getProject()).thenReturn(project);
        scm.getWorkspaceName(build, mock(Computer.class));
        
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(build, env );        
        assertEquals("The workspace name was incorrect", "WORKSPACE_SAMPLE", env.get(TeamFoundationServerScm.WORKSPACE_ENV_STR));
    }
    
    @Test
    public void assertWorksfolderPathIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "PATH", false, "WORKSPACE_SAMPLE", "user", "password");
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("WORKSPACE", "/this/is/a");
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The workfolder path was incorrect", "/this/is/a" + File.separator + "PATH", env.get(TeamFoundationServerScm.WORKFOLDER_ENV_STR));
    }
    
    @Test
    public void assertProjectPathIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "PATH", false, "WORKSPACE_SAMPLE", "user", "password");
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The project path was incorrect", "projectpath", env.get(TeamFoundationServerScm.PROJECTPATH_ENV_STR));
    }
    
    @Test
    public void assertServerUrlIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "PATH", false, "WORKSPACE_SAMPLE", "user", "password");
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The server URL was incorrect", "serverurl", env.get(TeamFoundationServerScm.SERVERURL_ENV_STR));
    }
    
    @Test
    public void assertTfsUserNameIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "PATH", false, "WORKSPACE_SAMPLE", "user", "password");
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The TFS user name was incorrect", "user", env.get(TeamFoundationServerScm.USERNAME_ENV_STR));
    }
    
    /**
     * Workspace name must be less than 64 characters, cannot end with a space or period, and cannot contain any of the following characters: "/:<>|*?
     */
    @Test
    public void assertWorkspaceNameReplacesInvalidChars() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, ".", false, "A\"B/C:D<E>F|G*H?I", "user", "password");
        assertEquals("Workspace name contained invalid chars", "A_B_C_D_E_F_G_H_I", scm.getWorkspaceName(null, null));
    }
    
    /**
     * Workspace name must be less than 64 characters, cannot end with a space or period, and cannot contain any of the following characters: "/:<>|*?
     */
    @Test
    public void assertWorkspaceNameReplacesEndingPeriod() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, ".", false, "Workspace.Name.", "user", "password");
        assertEquals("Workspace name ends with period", "Workspace.Name_", scm.getWorkspaceName(null, null));
    }
    
    /**
     * Workspace name must be less than 64 characters, cannot end with a space or period, and cannot contain any of the following characters: "/:<>|*?
     */
    @Test
    public void assertWorkspaceNameReplacesEndingSpace() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, ".", false, "Workspace Name ", "user", "password");
        assertEquals("Workspace name ends with space", "Workspace Name_", scm.getWorkspaceName(null, null));
    }    
    
    @Test public void assertServerUrlResolvesBuildVariables() {
        ParametersAction action = mock(ParametersAction.class);
        when(action.substitute(isA(AbstractBuild.class), isA(String.class))).thenReturn("https://RESOLVED.com");
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getAction(ParametersAction.class)).thenReturn(action);

        TeamFoundationServerScm scm = new TeamFoundationServerScm("https://${PARAM}.com", null, ".", false, "", "user", "password");
        assertEquals("The server url wasnt resolved", "https://RESOLVED.com", scm.getServerUrl(build));
    }    
    
    @Test public void assertProjectPathResolvesBuildVariables() {
        ParametersAction action = mock(ParametersAction.class);
        when(action.substitute(isA(AbstractBuild.class), isA(String.class))).thenReturn("$/RESOLVED/path");
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getAction(ParametersAction.class)).thenReturn(action);

        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, "$/$PARAM/path", ".", false, "", "user", "password");
        assertEquals("The project path wasnt resolved", "$/RESOLVED/path", scm.getProjectPath(build));
    }    
    
    @Test public void assertWorkspaceNameResolvesBuildVariables() {
        ParametersAction action = mock(ParametersAction.class);
        when(action.substitute(isA(AbstractBuild.class), isA(String.class))).thenReturn("WS-RESOLVED");
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getAction(ParametersAction.class)).thenReturn(action);

        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, ".", false, "WS-${PARAM}", "user", "password");
        assertEquals("The workspace name wasnt resolved", "WS-RESOLVED", scm.getWorkspaceName(build, mock(Computer.class)));
    }
    
    @Test public void assertTfsWorkspaceIsntRemovedIfThereIsNoBuildWhenProcessWorkspaceBeforeDeletion() throws Exception {
        AbstractProject project = mock(AbstractProject.class);
        Node node = mock(Node.class);
        TeamFoundationServerScm scm = new TeamFoundationServerScm("server", "projectpath", ".", false, "workspace", "user", "password");
        assertThat(scm.processWorkspaceBeforeDeletion(project, workspace, node), is(true));
        verify(project).getLastBuild();
        verifyNoMoreInteractions(project);
    }

    @Test public void assertWorkspaceIsntRemoveIfThereIsNoBuildOnSpecifiedNodeAndHudsonWantsToRemoveWorkspaceOnNode() throws Exception {
        AbstractProject project = mock(AbstractProject.class);
        AbstractBuild build = mock(AbstractBuild.class);
        Node node = mock(Node.class);
        Node inNode = mock(Node.class);
        when(project.getLastBuild()).thenReturn(build);
        when(build.getPreviousBuild()).thenReturn(build).thenReturn(null);
        when(build.getBuiltOn()).thenReturn(node).thenReturn(node);
        when(node.getNodeName()).thenReturn("node1").thenReturn("node2");
        when(inNode.getNodeName()).thenReturn("needleNode").thenReturn("needleNode");
        TeamFoundationServerScm scm = new TeamFoundationServerScm("server", "projectpath", ".", false, "workspace", "user", "password");
        assertThat( scm.processWorkspaceBeforeDeletion(project, workspace, inNode), is(true));
        verify(project).getLastBuild();
        verify(node, times(2)).getNodeName();
        verify(build, times(2)).getBuiltOn();
        verify(build, times(2)).getPreviousBuild();

        verifyNoMoreInteractions(project);
        verifyNoMoreInteractions(node);
        verifyNoMoreInteractions(build);        
    }
}
