package hudson.plugins.tfs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.FilePath;
import hudson.model.AbstractProject;

import org.junit.After;
import org.junit.Test;


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
        AbstractProject<?, ?> project = mock(AbstractProject.class);
        stub(project.getName()).toReturn("ThisIsAJob");
        
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, ".", false, "erik_${JOB_NAME}", "user", "password");
        assertEquals("Workspace name was incorrect", "erik_ThisIsAJob", scm.getNormalizedWorkspaceName(project));
    }
    
    @Test 
    public void assertDoUsernameCheckRegexWorks() {
        assertFalse("redsolo".matches(TeamFoundationServerScm.DescriptorImpl.DOMAIN_SLASH_USER_REGEX));
        assertTrue("snd\\redsolo".matches(TeamFoundationServerScm.DescriptorImpl.DOMAIN_SLASH_USER_REGEX));
        assertFalse("redsolo".matches(TeamFoundationServerScm.DescriptorImpl.USER_AT_DOMAIN_REGEX));
        assertTrue("redsolo@snd".matches(TeamFoundationServerScm.DescriptorImpl.USER_AT_DOMAIN_REGEX));
    }
    
    @Test 
    public void assertDoProjectPathCheckRegexWorks() {
        assertFalse("Project path regex matched an invalid project path", "tfsandbox".matches(TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX));
        assertFalse("Project path regex matched an invalid project path", "tfsandbox/with/sub/pathes".matches(TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX));
        assertFalse("Project path regex matched an invalid project path", "tfsandbox$/with/sub/pathes".matches(TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX));
        assertTrue("Project path regex did not match a valid project path", "$/tfsandbox".matches(TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX));
        assertTrue("Project path regex did not match a valid project path", "$/tfsandbox/path with space/subpath".matches(TeamFoundationServerScm.DescriptorImpl.PROJECT_PATH_REGEX));
    }
    
    @Test
    public void assertDefaultValueIsUsedForEmptyLocalPath() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "", false, "workspace", "user", "password");
        assertEquals("Default value for work folder was incorrect", ".", scm.getLocalPath());
    }
    
    @Test
    public void assertDefaultValueIsUsedForEmptyWorkspaceName() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", ".", false, "", "user", "password");
        assertEquals("Default value for workspace was incorrect", "Hudson-${JOB_NAME}", scm.getWorkspaceName());
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
}
