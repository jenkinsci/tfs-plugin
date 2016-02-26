package hudson.plugins.tfs.browsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.tfs.TeamFoundationServerScm;
import hudson.plugins.tfs.model.ChangeLogSet;
import hudson.plugins.tfs.model.ChangeSet;

import java.net.URL;

import hudson.util.Secret;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;

@SuppressWarnings("rawtypes")
public class TeamSystemWebAccessBrowserTest {

    @Test public void assertChangeSetLinkWithServerUrlWithPort() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        URL actual = browser.getChangeSetLink(changeSet);
        assertEquals("The change set link was incorrect", "http://tfs:8080/_versionControl/changeset?id=99", actual.toString());
    }

	@Bug(7394)
	@Test
	public void assertChangeSetLinkWithOnlyServerUrl() throws Exception {
		TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs");
		ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
		URL actual = browser.getChangeSetLink(changeSet);
		assertEquals("The change set link was incorrect", "http://tfs/_versionControl/changeset?id=99", actual.toString());
	}

	@Bug(7394)
	@Test
	public void assertChangeSetLinkWithOnlyServerUrlWithTrailingSlash() throws Exception {
		TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs/");
		ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
		URL actual = browser.getChangeSetLink(changeSet);
		assertEquals("The change set link was incorrect","http://tfs/_versionControl/changeset?id=99", actual.toString());
	}
    
    @Test public void assertChangeSetLinkUsesScmConfiguration() throws Exception {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?,?> project = mock(AbstractProject.class);
        when(build.getProject()).thenReturn(project);
        when(project.getScm()).thenReturn(new TeamFoundationServerScm("http://server:80", null, null, null, false, null, null, (Secret) null));
        
        ChangeSet changeset = new ChangeSet("62643", null, "user", "comment");
        new ChangeLogSet(build, new ChangeSet[]{ changeset});        
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("");
        URL actual = browser.getChangeSetLink(changeset);
        assertEquals("The change set link was incorrect", "http://server:80/_versionControl/changeset?id=62643", actual.toString());
    }

    @Test public void assertFileLink() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "add");
        changeSet.add(item);
        URL actual = browser.getFileLink(item);
        assertEquals("The change set link was incorrect", "http://tfs:8080/_versionControl/changeset?id=99#path=%24%2FProject%2FFolder%2Ffile.cs&_a=contents", actual.toString());
    }

    @Test public void assertDiffLink() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "edit");
        changeSet.add(item);
        URL actual = browser.getDiffLink(item);
        assertEquals("The change set link was incorrect", "http://tfs:8080/_versionControl/changeset?id=99#path=%24%2FProject%2FFolder%2Ffile.cs&_a=compare", actual.toString());
    }

    @Test public void assertNullDiffLinkForAddedFile() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "add");
        changeSet.add(item);
        assertNull("The diff link should be null for new files", browser.getDiffLink(item));
    }
    
    @Test public void assertDescriptorBaseUrlRemovesName() throws Exception {
        String expected = TeamSystemWebAccessBrowser.DescriptorImpl.getBaseUrl("http://server:80/UI/Pages/Scc/ViewChangeset.aspx?changeset=62643");
        assertEquals("The base url was incorrect", "http://server:80/UI/Pages/Scc/", expected);
    }
    
    @Test public void assertDescriptorBaseUrlDoesNotRemoveLastPath() throws Exception {
        String expected = TeamSystemWebAccessBrowser.DescriptorImpl.getBaseUrl("http://server:80/UI/Pages/Scc/");
        assertEquals("The base url was incorrect", "http://server:80/UI/Pages/Scc/", expected);
    }
}
