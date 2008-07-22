package hudson.plugins.tfs.browsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.tfs.TeamFoundationServerScm;
import hudson.plugins.tfs.model.ChangeLogSet;
import hudson.plugins.tfs.model.ChangeSet;

import java.net.URL;

import org.junit.Test;

@SuppressWarnings("unchecked")
public class TeamSystemWebAccessBrowserTest {

    /**
     * http://server:port/UI/Pages/Scc/ViewChangeset.aspx?changeset=62643
     */
    @Test public void assertChangeSetLink() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://server:80/UI/Pages/Scc/ViewChangeset.aspx?changeset=6200");
        ChangeSet changeSet = new ChangeSet("62643", null, "user", "comment");
        URL actual = browser.getChangeSetLink(changeSet);
        assertEquals("The change set link was incorrect", "http://server:80/UI/Pages/Scc/ViewChangeset.aspx?changeset=62643", actual.toString());
    }
    
    @Test public void assertChangeSetLinkUsesScmConfiguration() throws Exception {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?,?> project = mock(AbstractProject.class);
        stub(build.getProject()).toReturn(project);
        stub(project.getScm()).toReturn(new TeamFoundationServerScm("http://server:80", null, null, false, null, null, null));
        
        ChangeSet changeset = new ChangeSet("62643", null, "user", "comment");
        new ChangeLogSet(build, new ChangeSet[]{ changeset});        
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("");
        URL actual = browser.getChangeSetLink(changeset);
        assertEquals("The change set link was incorrect", "http://server:80/UI/Pages/Scc/ViewChangeset.aspx?changeset=62643", actual.toString());
    }
    
    @Test public void assertDescriptorBaseUrlRemovesName() {
        String expected = TeamSystemWebAccessBrowser.DescriptorImpl.getBaseUrl("http://server:80/UI/Pages/Scc/ViewChangeset.aspx?changeset=62643");
        assertEquals("The base url was incorrect", "http://server:80/UI/Pages/Scc/", expected);
    }
    
    @Test public void assertDescriptorBaseUrlDoesNotRemoveLastPath() {
        String expected = TeamSystemWebAccessBrowser.DescriptorImpl.getBaseUrl("http://server:80/UI/Pages/Scc/");
        assertEquals("The base url was incorrect", "http://server:80/UI/Pages/Scc/", expected);
    }
}
