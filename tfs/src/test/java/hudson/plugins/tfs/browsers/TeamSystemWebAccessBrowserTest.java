package hudson.plugins.tfs.browsers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.tfs.TeamFoundationServerScm;
import hudson.plugins.tfs.model.ChangeLogSet;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.ChangeSet.Item;

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
        assertEquals("The change set link was incorrect", "http://tfs:8080/_versionControl/changeset/99", actual.toString());
    }

    @Test public void assertChangeSetLinkWithRealisticServerUrlWithPort() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/tfs/coll");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        URL actual = browser.getChangeSetLink(changeSet);
        assertEquals("The change set link was incorrect", "http://tfs:8080/tfs/coll/_versionControl/changeset/99", actual.toString());
    }

    @Test public void assertChangeSetLinkWithRealisticServerUrl() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs/tfs/coll");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        URL actual = browser.getChangeSetLink(changeSet);
        assertEquals("The change set link was incorrect", "http://tfs/tfs/coll/_versionControl/changeset/99", actual.toString());
    }

	@Bug(7394)
	@Test
	public void assertChangeSetLinkWithOnlyServerUrl() throws Exception {
		TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs");
		ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
		URL actual = browser.getChangeSetLink(changeSet);
		assertEquals("The change set link was incorrect", "http://tfs/_versionControl/changeset/99", actual.toString());
	}

	@Bug(7394)
	@Test
	public void assertChangeSetLinkWithOnlyServerUrlWithTrailingSlash() throws Exception {
		TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs/");
		ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
		URL actual = browser.getChangeSetLink(changeSet);
		assertEquals("The change set link was incorrect","http://tfs/_versionControl/changeset/99", actual.toString());
	}
    
    private static TeamFoundationServerScm createTestScm(String serverUrl) {
        serverUrl = serverUrl != null ? serverUrl : "http://server:80/tfs/collection/";
        final String projectPath = "$/project/folder/folder/branch";
        final Secret password = null;
        final String userName = null;
        final TeamFoundationServerScm result = new TeamFoundationServerScm(serverUrl, projectPath, null, userName, password);

        final TeamFoundationServerRepositoryBrowser repositoryBrowser = mock(TeamFoundationServerRepositoryBrowser.class);
        result.setRepositoryBrowser(repositoryBrowser);

        return result;
    }

    @Test public void assertChangeSetLinkUsesScmConfiguration() throws Exception {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject<?,?> project = mock(AbstractProject.class);
        when(build.getProject()).thenReturn(project);
        final TeamFoundationServerScm testScm = createTestScm(null);
        when(project.getScm()).thenReturn(testScm);
        
        ChangeSet changeset = new ChangeSet("62643", null, "user", "comment");
        new ChangeLogSet(build, new ChangeSet[]{ changeset});        
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser(""); // configured but no URL specified

        URL actual = browser.getChangeSetLink(changeset);
        assertEquals("The change set link was incorrect", "http://server:80/tfs/collection/_versionControl/changeset/62643", actual.toString());
    }

    @Test public void assertChangeSetLinkUsesScmConfigurationNoSlash() throws Exception {
      AbstractBuild build = mock(AbstractBuild.class);
      AbstractProject<?,?> project = mock(AbstractProject.class);
      when(build.getProject()).thenReturn(project);
      // the server URL has not trailing slash...
      final TeamFoundationServerScm testScm = createTestScm("http://server:80/tfs/collection");
      when(project.getScm()).thenReturn(testScm);

      ChangeSet changeset = new ChangeSet("62643", null, "user", "comment");
      new ChangeLogSet(build, new ChangeSet[]{ changeset});
      TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser(""); // configured but no URL specified

      URL actual = browser.getChangeSetLink(changeset);
      assertEquals("The change set link was incorrect", "http://server:80/tfs/collection/_versionControl/changeset/62643", actual.toString());
    }

    @Test public void assertFileLinkUsesScmConfiguration() throws Exception {
      AbstractBuild build = mock(AbstractBuild.class);
      AbstractProject<?,?> project = mock(AbstractProject.class);
      when(build.getProject()).thenReturn(project);
      final TeamFoundationServerScm testScm = createTestScm(null);
      when(project.getScm()).thenReturn(testScm);

      ChangeSet changeset = new ChangeSet("62643", null, "user", "comment");
      ChangeSet.Item item = new Item("$/project/folder/folder/branch/some/path/to/some/file.txt", "action");
      changeset.add(item);
      new ChangeLogSet(build, new ChangeSet[]{ changeset});
      TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser(""); // configured but no URL specified

      URL actual = browser.getFileLink(item);
      assertEquals("The file link was incorrect", "http://server:80/tfs/collection/_versionControl/changeset/62643#path=%24%2Fproject%2Ffolder%2Ffolder%2Fbranch%2Fsome%2Fpath%2Fto%2Fsome%2Ffile.txt&version=62643&_a=contents", actual.toString());
    }

    @Test public void assertDiffLinkUsesScmConfiguration() throws Exception {
      AbstractBuild build = mock(AbstractBuild.class);
      AbstractProject<?,?> project = mock(AbstractProject.class);
      when(build.getProject()).thenReturn(project);
      final TeamFoundationServerScm testScm = createTestScm(null);
      when(project.getScm()).thenReturn(testScm);

      ChangeSet changeset = new ChangeSet("62643", null, "user", "comment");
      new ChangeLogSet(build, new ChangeSet[]{ changeset});
      ChangeSet.Item item = new Item("$/project/folder/folder/branch/some/path/to/some/file.txt", "action");
      changeset.add(item);

      TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser(""); // configured but no URL specified
      URL actual = browser.getDiffLink(item);
      assertEquals("The diff link was incorrect", "http://server:80/tfs/collection/_versionControl/changeset/62643#path=%24%2Fproject%2Ffolder%2Ffolder%2Fbranch%2Fsome%2Fpath%2Fto%2Fsome%2Ffile.txt&version=62643&_a=compare", actual.toString());
    }

    @Test public void assertFileLink() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "add");
        changeSet.add(item);
        URL actual = browser.getFileLink(item);
        assertEquals("The file link was incorrect", "http://tfs:8080/_versionControl/changeset/99#path=%24%2FProject%2FFolder%2Ffile.cs&version=99&_a=contents", actual.toString());
    }

    @Test public void assertFileLinkWithRealisticServerUrl() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/tfs/coll");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "add");
        changeSet.add(item);
        URL actual = browser.getFileLink(item);
        assertEquals("The file link was incorrect", "http://tfs:8080/tfs/coll/_versionControl/changeset/99#path=%24%2FProject%2FFolder%2Ffile.cs&version=99&_a=contents", actual.toString());
    }

    @Test public void assertDiffLink() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "edit");
        changeSet.add(item);
        URL actual = browser.getDiffLink(item);
        assertEquals("The diff link was incorrect", "http://tfs:8080/_versionControl/changeset/99#path=%24%2FProject%2FFolder%2Ffile.cs&version=99&_a=compare", actual.toString());
    }

    @Test public void assertDiffLinkWithRealisticServerUrl() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/tfs/coll");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "edit");
        changeSet.add(item);
        URL actual = browser.getDiffLink(item);
        assertEquals("The diff link was incorrect", "http://tfs:8080/tfs/coll/_versionControl/changeset/99#path=%24%2FProject%2FFolder%2Ffile.cs&version=99&_a=compare", actual.toString());
    }

    @Test public void assertNullDiffLinkForAddedFile() throws Exception {
        TeamSystemWebAccessBrowser browser = new TeamSystemWebAccessBrowser("http://tfs:8080/");
        ChangeSet changeSet = new ChangeSet("99", null, "user", "comment");
        ChangeSet.Item item = new ChangeSet.Item("$/Project/Folder/file.cs", "add");
        changeSet.add(item);
        assertNull("The diff link should be null for new files", browser.getDiffLink(item));
    }
}
