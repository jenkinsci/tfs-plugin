package hudson.plugins.tfs.actions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.FilePath;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

import hudson.remoting.VirtualChannel;
import org.hamcrest.CustomMatcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CheckoutActionTest {

    private static final String MY_LABEL = "MyLabel";
	private FilePath hudsonWs;
    private @Mock Server server;
    private @Mock Workspaces workspaces;
    private @Mock Workspace workspace;
    private @Mock Project project;
    
    @Before public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        hudsonWs = Util.createTempFilePath();
    }

    @After public void teardown() throws Exception {
        if (hudsonWs != null) {
            hudsonWs.deleteRecursive();
        }
    }
    
    @Test
    public void assertFirstCheckoutBySingleVersionSpecNotUsingUpdate() throws Exception {
    	when(server.getWorkspaces()).thenReturn(workspaces);
    	when(server.getProject("project")).thenReturn(project);
    	when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
    	when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
    	when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
    	
    	new CheckoutAction("workspace", "project", ".", false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
    	
    	verify(workspaces).newWorkspace("workspace");
    	verify(workspace).mapWorkfolder(project, hudsonWs.getRemote());
    	verify(project).getFiles(isA(String.class), eq(MY_LABEL));
    	verify(workspaces).deleteWorkspace(workspace);    	
    }
    
    @Test
    public void assertFirstCheckoutNotUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", false).checkout(server, hudsonWs,null, Util.getCalendar(2009, 9, 24));
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, hudsonWs.getRemote());
        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"));
        verify(workspaces).deleteWorkspace(workspace);
    }

    @Test
    public void assertFirstCheckoutBySingleVersionSpecUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists(new Workspace(server, "workspace"))).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", true).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, hudsonWs.getRemote());
        verify(project).getFiles(isA(String.class), eq(MY_LABEL));
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertFirstCheckoutUsingUpdate() throws Exception {
    	when(server.getWorkspaces()).thenReturn(workspaces);
    	when(server.getProject("project")).thenReturn(project);
    	when(workspaces.exists(new Workspace(server, "workspace"))).thenReturn(false);
    	when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
    	
    	new CheckoutAction("workspace", "project", ".", true).checkout(server, hudsonWs,null, Util.getCalendar(2009, 9, 24));
    	
    	verify(workspaces).newWorkspace("workspace");
    	verify(workspace).mapWorkfolder(project, hudsonWs.getRemote());
    	verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"));
    	verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }
    
    @Test
    public void assertSecondCheckoutBySingleVersionSpecUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", ".", true).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);

        verify(project).getFiles(isA(String.class), eq(MY_LABEL));
        verify(workspaces, never()).newWorkspace("workspace");
        verify(workspace, never()).mapWorkfolder(project, ".");
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }
    
    @Test
    public void assertSecondCheckoutUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", ".", true).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));

        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"));
        verify(workspaces, never()).newWorkspace("workspace");
        verify(workspace, never()).mapWorkfolder(project, ".");
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertSecondCheckoutBySingleVersionSpecNotUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);

        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, hudsonWs.getRemote());
        verify(project).getFiles(isA(String.class), eq(MY_LABEL));
        verify(workspaces).deleteWorkspace(workspace);
    }

    @Test
    public void assertSecondCheckoutNotUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", false).checkout(server, hudsonWs,null, Util.getCalendar(2009, 9, 24));

        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, hudsonWs.getRemote());
        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"));
        verify(workspaces).deleteWorkspace(workspace);
    }
   
    @Test
    public void assertDetailedHistoryIsNotRetrievedInFirstBuildCheckingOutByLabel() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", ".", true).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(project, never()).getDetailedHistory(isA(Calendar.class), isA(Calendar.class));
    }
   
    @Test
    public void assertDetailedHistoryIsNotRetrievedInFirstBuild() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", ".", true).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(project, never()).getDetailedHistory(isA(Calendar.class), isA(Calendar.class));
    }
    
    @Test
    public void assertDetailedHistoryIsRetrievedInSecondBuildCheckingOutByLabel() throws Exception {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getDetailedHistory(isA(String.class))).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        List<ChangeSet> actualList = action.checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        verify(project).getDetailedHistory(isA(String.class));
    }
    
    @Test
    public void assertDetailedHistoryIsRetrievedInSecondBuild() throws Exception {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getVCCHistory(isA(VersionSpec.class), isA(VersionSpec.class), anyBoolean(), anyInt())).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        final Calendar startDate = Util.getCalendar(2008, 9, 24);
        final Calendar endDate = Util.getCalendar(2008, 10, 24);
        List<ChangeSet> actualList = action.checkout(server, hudsonWs, startDate, endDate);
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        final DateVersionSpec startDateVersionSpec = new DateVersionSpec(startDate);
        verify(project).getVCCHistory(argThat(new DateVersionSpecMatcher(startDateVersionSpec)), isA(VersionSpec.class), eq(true), anyInt());
    }
    
    @Test
    public void assertWorkFolderIsCleanedIfNotUsingUpdate() throws Exception {
        hudsonWs.createTempFile("temp", "txt");
        FilePath tfsWs = hudsonWs.child("tfs-ws");
        tfsWs.mkdirs();
        tfsWs.createTempFile("temp", "txt");
        
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists(new Workspace(server, "workspace"))).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", "tfs-ws", false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        assertTrue("The local folder was removed", tfsWs.exists());
        assertEquals("The local TFS folder was not cleaned", 0, tfsWs.list((FileFilter)null).size());
        assertEquals("The Hudson workspace path was cleaned", 2, hudsonWs.list((FileFilter)null).size());
    }
    
    @Test
    public void assertWorkFolderIsCleanedIfNotUsingUpdateCheckingOutByLabel() throws Exception {
        hudsonWs.createTempFile("temp", "txt");
        FilePath tfsWs = hudsonWs.child("tfs-ws");
        tfsWs.mkdirs();
        tfsWs.createTempFile("temp", "txt");
        
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists(new Workspace(server, "workspace"))).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", "tfs-ws", false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        assertTrue("The local folder was removed", tfsWs.exists());
        assertEquals("The local TFS folder was not cleaned", 0, tfsWs.list((FileFilter)null).size());
        assertEquals("The Hudson workspace path was cleaned", 2, hudsonWs.list((FileFilter)null).size());
    }

    @Test
    public void assertWorkspaceIsNotCleanedIfUsingUpdate() throws Exception {
        FilePath tfsWs = hudsonWs.child("tfs-ws");
        tfsWs.mkdirs();
        tfsWs.createTempFile("temp", "txt");
        
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", "tfs-ws", true).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));

        assertTrue("The local folder was removed", tfsWs.exists());
        assertEquals("The TFS workspace path was cleaned", 1, hudsonWs.list((FileFilter)null).size());
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutBySingleVersionSpecDeletesWorkspaceAtStartIfNotUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verify(workspaces).deleteWorkspace(workspace);
        verify(workspaces).newWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDeletesWorkspaceAtStartIfNotUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verify(workspaces).deleteWorkspace(workspace);
        verify(workspaces).newWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDoesNotDeleteWorkspaceAtStartIfUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        
        new CheckoutAction("workspace", "project", ".", true).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutBySingleVersionSpecDoesNotDeleteWorkspaceAtStartIfUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        
        new CheckoutAction("workspace", "project", ".", true).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDoesNotDeleteWorkspaceIfNotUsingUpdateAndThereIsNoWorkspace() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(false).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        
        new CheckoutAction("workspace", "project", ".", false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).newWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutBySingleVersionSpecDoesNotDeleteWorkspaceIfNotUsingUpdateAndThereIsNoWorkspace() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(false).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        
        new CheckoutAction("workspace", "project", ".", false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).newWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(6596)
    @Test
    public void assertCheckoutOnlyRetrievesChangesToTheStartTimestampForCurrentBuild() throws Exception {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getVCCHistory(isA(VersionSpec.class), isA(VersionSpec.class), anyBoolean(), anyInt())).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        final Calendar startDate = Util.getCalendar(2008, 9, 24);
        final Calendar endDate = Util.getCalendar(2009, 9, 24);
        List<ChangeSet> actualList = action.checkout(server, hudsonWs, startDate, endDate);
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);

        final DateVersionSpec startDateVersionSpec = new DateVersionSpec(startDate);
        final DateVersionSpec endDateVersionSpec = new DateVersionSpec(endDate);
        verify(project).getVCCHistory(
                argThat(new DateVersionSpecMatcher(startDateVersionSpec)),
                argThat(new DateVersionSpecMatcher(endDateVersionSpec)),
                eq(true),
                anyInt());
        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"));
    }

    private static class DateVersionSpecMatcher extends CustomMatcher<DateVersionSpec> {

        private final DateVersionSpec base;

        public DateVersionSpecMatcher(final DateVersionSpec base) {
            super(base == null ? "(null)" : base.toString());
            this.base = base;
        }

        public boolean matches(final Object item) {
            if (base == null) {
                return item == null;
            }
            if (item != null && item instanceof DateVersionSpec) {
                final DateVersionSpec candidate = (DateVersionSpec) item;
                final Calendar baseDate = base.getDate();
                final Calendar candidateDate = candidate.getDate();
                return baseDate.equals(candidateDate);
            }
            return false;
        }
    }

    @Test
    public void determineCheckoutPath_absoluteOverrideOnNix() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String nixPath = "/opt/.jenkins/jobs/tfs-plugin/workspace";
        final FilePath workspacePath = new FilePath(vc, nixPath);
        final String localFolder = "/home/jenkins/tfs-plugin";

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, localFolder);

        Assert.assertEquals(localFolder, actual);
    }

    @Test
    public void determineCheckoutPath_absoluteOverrideOnWindows() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String windowsPath = "C:\\.jenkins\\jobs\\tfs-plugin\\workspace";
        final FilePath workspacePath = new FilePath(vc, windowsPath);
        final String localFolder = "C:\\Users\\Jenkins\\tfs-plugin";

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, localFolder);

        Assert.assertEquals(localFolder, actual);
    }

    @Test
    public void determineCheckoutPath_absoluteOverrideOnWindowsWithForwardSlashes() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String windowsPath = "C:/.jenkins/jobs/tfs-plugin/workspace";
        final FilePath workspacePath = new FilePath(vc, windowsPath);
        final String localFolder = "C:/Users/Jenkins/tfs-plugin";

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, localFolder);

        Assert.assertEquals(localFolder, actual);
    }

    @Test
    public void determineCheckoutPath_defaultOnNix() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String nixPath = "/opt/.jenkins/jobs/tfs-plugin/workspace";
        final FilePath workspacePath = new FilePath(vc, nixPath);

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, ".");

        Assert.assertEquals(nixPath, actual);
    }

    @Test
    public void determineCheckoutPath_defaultOnWindows() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String windowsPath = "C:\\.jenkins\\jobs\\tfs-plugin\\workspace";
        final FilePath workspacePath = new FilePath(vc, windowsPath);

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, ".");

        Assert.assertEquals(windowsPath, actual);
    }

    @Test
    public void determineCheckoutPath_defaultOnWindowsWithForwardSlashes() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String windowsPath = "C:/.jenkins/jobs/tfs-plugin/workspace";
        final FilePath workspacePath = new FilePath(vc, windowsPath);

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, ".");

        Assert.assertEquals(windowsPath, actual);
    }

    @Test
    public void determineCheckoutPath_relativeOnNix() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String nixPath = "/opt/.jenkins/jobs/tfs-plugin/workspace";
        final FilePath workspacePath = new FilePath(vc, nixPath);

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, "../files");

        Assert.assertEquals("/opt/.jenkins/jobs/tfs-plugin/files", actual);
    }

    @Test
    public void determineCheckoutPath_relativeOnWindows() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String windowsPath = "C:\\.jenkins\\jobs\\tfs-plugin\\workspace";
        final FilePath workspacePath = new FilePath(vc, windowsPath);

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, "..\\files");

        Assert.assertEquals("C:\\.jenkins\\jobs\\tfs-plugin\\files", actual);
    }

    @Test
    public void determineCheckoutPath_relativeOnWindowsWithForwardSlashes() {
        final VirtualChannel vc = mock(VirtualChannel.class);
        final String windowsPath = "C:/.jenkins/jobs/tfs-plugin/workspace";
        final FilePath workspacePath = new FilePath(vc, windowsPath);

        final String actual = CheckoutAction.determineCheckoutPath(workspacePath, "../files");

        Assert.assertEquals("C:/.jenkins/jobs/tfs-plugin/files", actual);
    }
}
