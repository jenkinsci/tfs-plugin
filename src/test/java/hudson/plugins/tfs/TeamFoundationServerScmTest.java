package hudson.plugins.tfs;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.model.AbstractProject;

import org.junit.Test;


public class TeamFoundationServerScmTest {

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
    public void assertDefaultValueIsUsedForEmptyLocalPath() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", "", false, "workspace", "user", "password");
        assertEquals("Default value for work folder was incorrect", ".", scm.getLocalPath());
    }
    
    @Test
    public void assertDefaultValueIsUsedForEmptyWorkspaceName() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", ".", false, "", "user", "password");
        assertEquals("Default value for workspace was incorrect", "Hudson-${JOB_NAME}", scm.getWorkspaceName());
    }
}
