package hudson.plugins.tfs;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.thoughtworks.xstream.XStream;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Node;

import hudson.plugins.tfs.model.Project;
import hudson.util.Secret;
import hudson.util.SecretOverride;
import hudson.util.XStream2;
import org.junit.After;
import org.junit.Assert;
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

    /**
     Up until version 3.1.1, the plugin used to store the password in a base64-encoded string.
     As of bd98b91ea614c307a6bb1e0af36d9dd2a5646e29, an encrypted version of the password is stored.
     This test makes sure a job can be upgraded without loss of the password.
     */
    @Test public void upgradeFromScrambledPassword() {
        SecretOverride secretOverride = null;
        try {
            secretOverride = new SecretOverride();
            final String xmlString =
                    "<scm class='hudson.plugins.tfs.TeamFoundationServerScm' plugin='tfs@3.1.1'>\n" +
                    "    <serverUrl>http://example.tfs.server.invalid:8080/tfs</serverUrl>\n" +
                    "    <projectPath>$/example/path</projectPath>\n" +
                    "    <localPath>.</localPath>\n" +
                    "    <workspaceName>Hudson-${JOB_NAME}-${NODE_NAME}</workspaceName>\n" +
                    "    <userPassword>ZXhhbXBsZVBhc3N3b3Jk</userPassword>\n" +
                    "    <userName>example\\tfsbuilder</userName>\n" +
                    "    <useUpdate>false</useUpdate>\n" +
                    "</scm>";
            final XStream serializer = new XStream2();

            final TeamFoundationServerScm tfsScmObject = (TeamFoundationServerScm) serializer.fromXML(xmlString);

            final String actual = tfsScmObject.getUserPassword();
            assertEquals("examplePassword", actual);
            assertEquals("examplePassword", Secret.toString(tfsScmObject.getPassword()));

            final String expectedUpgradedXml =
                    "<hudson.plugins.tfs.TeamFoundationServerScm>\n" +
                            "  <serverUrl>http://example.tfs.server.invalid:8080/tfs</serverUrl>\n" +
                            "  <projectPath>$/example/path</projectPath>\n" +
                            "  <localPath>.</localPath>\n" +
                            "  <workspaceName>Hudson-${JOB_NAME}-${NODE_NAME}</workspaceName>\n" +
                            "  <password>zs+99bxCGlcSxR3Umnj0q0OjYXVSiB+qLzS0ZjuHz2M=</password>\n" +
                            "  <userName>example\\tfsbuilder</userName>\n" +
                            "  <useUpdate>false</useUpdate>\n" +
                            "</hudson.plugins.tfs.TeamFoundationServerScm>";

            final String actualUpgradedXml = serializer.toXML(tfsScmObject);

            assertEquals(expectedUpgradedXml, actualUpgradedXml);

            final TeamFoundationServerScm tfsScmObject2 = (TeamFoundationServerScm) serializer.fromXML(actualUpgradedXml);
            final String actual2 = tfsScmObject2.getUserPassword();
            assertEquals("examplePassword", actual2);
            assertEquals("examplePassword", Secret.toString(tfsScmObject.getPassword()));
        }
        finally {
            if (secretOverride != null) {
                try {
                    secretOverride.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
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
    public void assertCloakedPathsCheckRegexWorks() {

        final String shouldNotMatch = "Cloaked paths regex matched an invalid cloaked path";
        assertFalse(shouldNotMatch, isCloakedPathValid("tfsandbox"));
        assertFalse(shouldNotMatch, isCloakedPathValid("tfsandbox/with/sub/pathes"));
        assertFalse(shouldNotMatch, isCloakedPathValid("tfsandbox$/with/sub/pathes"));
        assertFalse(shouldNotMatch, isCloakedPathValid("$/tfsandbox/path1;$/tfsandbox/path2"));
        assertFalse(shouldNotMatch, isCloakedPathValid("$/tfsandbox/path1 ; $/tfsandbox/path2 ; $/tfsandbox/path3"));
        assertFalse(shouldNotMatch, isCloakedPathValid("$/foo/;$/bar/;$/baz/"));
        assertFalse(shouldNotMatch, isCloakedPathValid("$/foo/;\n$/bar/;\n$/baz/"));

        final String shoudMatch = "Cloaked paths regex did not match a valid cloaked path";
        assertTrue(shoudMatch, isCloakedPathValid("$/tfsandbox"));
        assertTrue(shoudMatch, isCloakedPathValid("$/tfsandbox/path with space/subpath"));
        assertTrue(shoudMatch, isCloakedPathValid("$/tfsandbox/with/${parameter}/path"));
        assertTrue(shoudMatch, isCloakedPathValid("$/foo/\n$/bar/\n$/baz/"));
        assertTrue(shoudMatch, isCloakedPathValid(" $/foo/ \n $/bar/ \n $/baz/ "));
        assertTrue(shoudMatch, isCloakedPathValid("\n$/foo/\n\n$/bar/\n\n$/baz/\n"));
    }

    private static boolean isCloakedPathValid(final String path) {
        return TeamFoundationServerScm.DescriptorImpl.CLOAKED_PATHS_REGEX.matcher(path).matches();
    }

    @Test
    public void serializeCloakedPathCollectionToString_one() {
        final List<String> cloakedPaths = Collections.singletonList("$/foo");

        final String actual = TeamFoundationServerScm.serializeCloakedPathCollectionToString(cloakedPaths);

        Assert.assertEquals("$/foo", actual);
    }

    @Test
    public void serializeCloakedPathCollectionToString_two() {
        final List<String> cloakedPaths = Arrays.asList("$/foo", "$/bar");

        final String actual = TeamFoundationServerScm.serializeCloakedPathCollectionToString(cloakedPaths);

        Assert.assertEquals("$/foo\n$/bar", actual);
    }

    @Test
    public void serializeCloakedPathCollectionToString_many() {
        final List<String> cloakedPaths = Arrays.asList("$/foo/", "$/bar/", "$/baz/");

        final String actual = TeamFoundationServerScm.serializeCloakedPathCollectionToString(cloakedPaths);

        Assert.assertEquals("$/foo/\n$/bar/\n$/baz/", actual);
    }

    @Test
    public void splitCloakedPaths_one() {
        final String input = "$/foo/";

        final Collection<String> actual = TeamFoundationServerScm.splitCloakedPaths(input);

        areEqual(actual, input);
    }

    @Test
    public void splitCloakedPaths_newlinesMany() {
        final String input = "$/foo/\n$/bar/\n$/baz/";

        final Collection<String> actual = TeamFoundationServerScm.splitCloakedPaths(input);

        areEqual(actual, "$/foo/", "$/bar/", "$/baz/");
    }

    @Test
    public void splitCloakedPaths_newlinesWithLiberalSpacing() {
        final String input = " $/foo/ \n $/bar/ \n $/baz/ ";

        final Collection<String> actual = TeamFoundationServerScm.splitCloakedPaths(input);

        areEqual(actual, "$/foo/", "$/bar/", "$/baz/");
    }

    @Test
    public void splitCloakedPaths_newlinesWithBlankLines() {
        final String input = "\n$/foo/\n\n$/bar/\n\n$/baz/\n";

        final Collection<String> actual = TeamFoundationServerScm.splitCloakedPaths(input);

        areEqual(actual, "$/foo/", "$/bar/", "$/baz/");
    }

    private static <T> void areEqual(final Collection<T> actual, T... expected) {
        final Iterator<T> ai = actual.iterator();
        int ei = 0;
        while (ei < expected.length && ai.hasNext()) {
            final T expectedItem = expected[ei];
            final T actualItem = ai.next();
            Assert.assertEquals(expectedItem, actualItem);
            ei++;
        }
        if (ei == expected.length) {
            if (ai.hasNext()) {
                Assert.fail("There were more elements than expected");
            }
        }
        else {
            if (!ai.hasNext()) {
                Assert.fail("Some elements were missing from actual.");
            }
        }
    }

    @Test
    public void assertDefaultValueIsUsedForEmptyLocalPath() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "", false, "workspace");
        assertEquals("Default value for work folder was incorrect", ".", scm.getLocalPath());
    }
    
    @Test
    public void assertDefaultValueIsUsedForEmptyWorkspaceName() {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, ".", false, "");
        assertEquals("Default value for workspace was incorrect", "Hudson-${JOB_NAME}-${NODE_NAME}", scm.getWorkspaceName());
    }
    
    @Test
    public void assertGetModuleRootReturnsWorkFolder() throws Exception {
        workspace = Util.createTempFilePath();
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "workfolder", false, "");
        FilePath moduleRoot = scm.getModuleRoot(workspace);
        assertEquals("Name for module root was incorrect", "workfolder", moduleRoot.getName());
        assertEquals("The parent for module root was incorrect", workspace.getName(), moduleRoot.getParent().getName());
    }
    
    @Test
    public void assertGetModuleRootWorksForDotWorkFolder() throws Exception {
        workspace = Util.createTempFilePath();
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, ".", false, "");
        FilePath moduleRoot = scm.getModuleRoot(workspace);
        assertTrue("The module root was reported as not existing even if its virtually the same as workspace",
                moduleRoot.exists());
        assertEquals("The module root was not the same as workspace", moduleRoot.lastModified(), workspace.lastModified());
    }
    
    @Test
    public void assertWorkspaceNameIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, ".", false, "WORKSPACE_SAMPLE");
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject project = mock(AbstractProject.class);
        when(build.getProject()).thenReturn(project);
        
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(build, env);
        assertEquals("The workspace name was incorrect", "WORKSPACE_SAMPLE", env.get(TeamFoundationServerScm.WORKSPACE_ENV_STR));
    }
    
    @Test
    public void assertWorksfolderPathIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "PATH", false, "WORKSPACE_SAMPLE");
        
        Map<String, String> env = new HashMap<String, String>();
        env.put("WORKSPACE", "/this/is/a");
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The workfolder path was incorrect", "/this/is/a" + File.separator + "PATH", env.get(TeamFoundationServerScm.WORKFOLDER_ENV_STR));
    }
    
    @Test
    public void assertProjectPathIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "PATH", false, "WORKSPACE_SAMPLE");
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The project path was incorrect", "projectpath", env.get(TeamFoundationServerScm.PROJECTPATH_ENV_STR));
    }
    
    @Test
    public void assertServerUrlIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "PATH", false, "WORKSPACE_SAMPLE");
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The server URL was incorrect", "serverurl", env.get(TeamFoundationServerScm.SERVERURL_ENV_STR));
    }
    
    @Test
    public void assertTfsUserNameIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "PATH", false, "WORKSPACE_SAMPLE", "user", (Secret) null);
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("The TFS user name was incorrect", "user", env.get(TeamFoundationServerScm.USERNAME_ENV_STR));
    }
    
    @Test
    public void assertTfsWorkspaceChangesetIsAddedToEnvVars() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "PATH", false, "WORKSPACE_SAMPLE");
        scm.setWorkspaceChangesetVersion("12345");
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("Workspace changeset version was incorrect", "12345", env.get(TeamFoundationServerScm.WORKSPACE_CHANGESET_ENV_STR));
    }
  
    @Test
    public void assertTfsWorkspaceChangesetIsNotAddedToEnvVarsIfEmpty() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "PATH", false, "WORKSPACE_SAMPLE");
        scm.setWorkspaceChangesetVersion("");
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("Workspace changeset version was not null", null, env.get(TeamFoundationServerScm.WORKSPACE_CHANGESET_ENV_STR));
    }

    @Test
    public void assertTfsWorkspaceChangesetIsNotAddedToEnvVarsIfNull() throws Exception {
        TeamFoundationServerScm scm = new TeamFoundationServerScm("serverurl", "projectpath", null, "PATH", false, "WORKSPACE_SAMPLE");
        scm.setWorkspaceChangesetVersion(null);
        Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(mock(AbstractBuild.class), env );        
        assertEquals("Workspace changeset version was not null", null, env.get(TeamFoundationServerScm.WORKSPACE_CHANGESET_ENV_STR));
    }

    @Test public void recordWorkspaceChangesetVersion() throws Exception {
        final TeamFoundationServerScm scm = new TeamFoundationServerScm("serverUrl", "projectPath", null, "localPath", false, "workspace");
        final AbstractBuild build = mock(AbstractBuild.class);
        when(build.getTimestamp()).thenReturn(new GregorianCalendar(2015, 03, 28, 22, 04));
        final BuildListener listener = null;
        final Project project = mock(Project.class);
        when(project.getRemoteChangesetVersion(isA(VersionSpec.class))).thenReturn(42);
        final String projectPath = "projectPath";
        final String singleVersionSpec = null;

        final int actual = scm.recordWorkspaceChangesetVersion(build, listener, project, projectPath, singleVersionSpec);

        Assert.assertEquals(42, actual);
        final Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(build, env);
        assertEquals("42", env.get(TeamFoundationServerScm.WORKSPACE_CHANGESET_ENV_STR));
    }

    @Test public void recordWorkspaceChangesetVersionWithSingleVersionSpec() throws Exception {
        final TeamFoundationServerScm scm = new TeamFoundationServerScm("serverUrl", "projectPath", null, "localPath", false, "workspace");
        final AbstractBuild build = mock(AbstractBuild.class);
        final BuildListener listener = null;
        final Project project = mock(Project.class);
        when(project.getRemoteChangesetVersion(isA(VersionSpec.class))).thenReturn(42);
        final String projectPath = "projectPath";
        final String singleVersionSpec = "Lfoo";

        final int actual = scm.recordWorkspaceChangesetVersion(build, listener, project, projectPath, singleVersionSpec);

        Assert.assertEquals(42, actual);
        final Map<String, String> env = new HashMap<String, String>();
        scm.buildEnvVars(build, env);
        assertEquals("42", env.get(TeamFoundationServerScm.WORKSPACE_CHANGESET_ENV_STR));
    }

    /**
     * Workspace name must be less than 64 characters, cannot end with a space or period, and cannot contain any of the following characters: "/:<>|*?
     */
    @Test
    public void assertWorkspaceNameReplacesInvalidChars() {
        EnvVars env = new EnvVars();
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, null, ".", false, "A\"B/C:D<E>F|G*H?I");
        assertEquals("Workspace name contained invalid chars", "A_B_C_D_E_F_G_H_I", scm.getWorkspaceName(env));
    }
    
    /**
     * Workspace name must be less than 64 characters, cannot end with a space or period, and cannot contain any of the following characters: "/:<>|*?
     */
    @Test
    public void assertWorkspaceNameReplacesEndingPeriod() {
        EnvVars env = new EnvVars();
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, null, ".", false, "Workspace.Name.");
        assertEquals("Workspace name ends with period", "Workspace.Name_", scm.getWorkspaceName(env));
    }
    
    /**
     * Workspace name must be less than 64 characters, cannot end with a space or period, and cannot contain any of the following characters: "/:<>|*?
     */
    @Test
    public void assertWorkspaceNameReplacesEndingSpace() {
        EnvVars env = new EnvVars();
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, null, ".", false, "Workspace Name ");
        assertEquals("Workspace name ends with space", "Workspace Name_", scm.getWorkspaceName(env));
    }    
    
    @Test public void assertServerUrlResolvesBuildVariables() {
        EnvVars env = new EnvVars();
        env.put("PARAM", "RESOLVED");
        TeamFoundationServerScm scm = new TeamFoundationServerScm("https://${PARAM}.com", null, null, ".", false, "");
        assertEquals("The server url wasnt resolved", "https://RESOLVED.com", scm.getServerUrl(env));
    }    
    
    @Test public void assertProjectPathResolvesBuildVariables() {
        EnvVars env = new EnvVars();
        env.put("PARAM", "RESOLVED");
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, "$/$PARAM/path", null, ".", false, "");
        assertEquals("The project path wasnt resolved", "$/RESOLVED/path", scm.getProjectPath(env));
    }    
    
    @Test public void assertWorkspaceNameResolvesBuildVariables() {
        EnvVars env = new EnvVars();
        env.put("PARAM", "RESOLVED");
        TeamFoundationServerScm scm = new TeamFoundationServerScm(null, null, null, ".", false, "WS-${PARAM}");
        assertEquals("The workspace name wasnt resolved", "WS-RESOLVED", scm.getWorkspaceName(env));
    }
    
    @Test public void assertTfsWorkspaceIsntRemovedIfThereIsNoBuildWhenProcessWorkspaceBeforeDeletion() throws Exception {
        AbstractProject project = mock(AbstractProject.class);
        Node node = mock(Node.class);
        TeamFoundationServerScm scm = new TeamFoundationServerScm("server", "projectpath", null, ".", false, "workspace");
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
        TeamFoundationServerScm scm = new TeamFoundationServerScm("server", "projectpath", null, ".", false, "workspace");
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
