package hudson.plugins.tfs.actions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.microsoft.tfs.core.clients.versioncontrol.specs.version.DateVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import hudson.FilePath;
import hudson.model.TaskListener;
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

    private static final List<String> EMPTY_CLOAKED_PATHS_LIST = Collections.emptyList();

    private static final Map<String, String> EMPTY_MAPPED_PATHS_MAP = new TreeMap<String, String>();
    private static final Set<String> EMPTY_MAPPED_PATHS_LIST = EMPTY_MAPPED_PATHS_MAP.keySet();

    private static final String MY_LABEL = "MyLabel";
    private FilePath hudsonWs;
    private @Mock Server server;
    private @Mock Workspaces workspaces;
    private @Mock Workspace workspace;
    private @Mock Project project;
    private @Mock TaskListener taskListener;

    @Before public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        hudsonWs = Util.createTempFilePath();
    }

    @After public void teardown() throws Exception {
        if (hudsonWs != null) {
            hudsonWs.deleteRecursive();
        }
    }

    private void prepareCommonMocks() {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);

        when(server.getListener()).thenReturn(taskListener);
        when(taskListener.getLogger()).thenReturn(System.out);
        when(workspaces.getWorkspaceMapping(anyString())).thenReturn("workspace");
    }
    
    @Test
    public void assertFirstCheckoutBySingleVersionSpecNotUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(project.getProjectPath()).thenReturn("project");
    	when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
    	when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
    	
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);

        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
    	verify(project).getFiles(isA(String.class), eq(MY_LABEL), eq(false));
    	verify(workspaces).deleteWorkspace(workspace);    	
    }
    
    @Test
    public void assertFirstCheckoutNotUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(project.getProjectPath()).thenReturn("project");
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"), eq(false));
        verify(workspaces).deleteWorkspace(workspace);
    }

    @Test
    public void assertFirstCheckoutBySingleVersionSpecUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(project.getProjectPath()).thenReturn("project");
        when(workspaces.exists(new Workspace("workspace"))).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(project).getFiles(isA(String.class), eq(MY_LABEL), eq(false));
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertFirstCheckoutUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(project.getProjectPath()).thenReturn("project");
    	when(workspaces.exists(new Workspace("workspace"))).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
    	
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
    	
        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
    	verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"), eq(false));
    	verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }
    
    @Test
    public void assertSecondCheckoutBySingleVersionSpecUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);

        verify(project).getFiles(isA(String.class), eq(MY_LABEL), eq(false));
        verify(workspaces, never()).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }
    
    @Test
    public void assertSecondCheckoutUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));

        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"), eq(false));
        verify(workspaces, never()).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertSecondCheckoutBySingleVersionSpecNotUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(project.getProjectPath()).thenReturn("project");
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);

        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(project).getFiles(isA(String.class), eq(MY_LABEL), eq(false));
        verify(workspaces).deleteWorkspace(workspace);
    }

    @Test
    public void assertSecondCheckoutNotUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(project.getProjectPath()).thenReturn("project");
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));

        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"), eq(false));
        verify(workspaces).deleteWorkspace(workspace);
    }
   
    @Test
    public void assertDetailedHistoryIsNotRetrievedInFirstBuildCheckingOutByLabel() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(project, never()).getDetailedHistory(isA(Calendar.class), isA(Calendar.class));
    }
   
    @Test
    public void assertDetailedHistoryIsNotRetrievedInFirstBuild() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(project, never()).getDetailedHistory(isA(Calendar.class), isA(Calendar.class));
    }
    
    @Test
    public void assertDetailedHistoryIsRetrievedInSecondBuildCheckingOutByLabel() throws Exception {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getDetailedHistory(isA(String.class))).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false);
        List<ChangeSet> actualList = action.checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        verify(project).getDetailedHistory(isA(String.class));
    }
    
    @Test
    public void assertDetailedHistoryIsRetrievedInSecondBuild() throws Exception {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getDetailedHistoryWithoutCloakedPaths(isA(VersionSpec.class), isA(VersionSpec.class), anyCollection(), anyCollection())).thenReturn(list);

        CheckoutAction action = new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false);
        final Calendar startDate = Util.getCalendar(2008, 9, 24);
        final Calendar endDate = Util.getCalendar(2008, 10, 24);
        List<ChangeSet> actualList = action.checkout(server, hudsonWs, startDate, endDate);
        assertEquals("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        final DateVersionSpec startDateVersionSpec = new DateVersionSpec(startDate);
        verify(project).getDetailedHistoryWithoutCloakedPaths(argThat(new DateVersionSpecMatcher(startDateVersionSpec)), isA(VersionSpec.class), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_LIST));
    }
    
    @Test
    public void assertWorkFolderIsCleanedIfNotUsingUpdate() throws Exception {
        hudsonWs.createTempFile("temp", "txt");
        FilePath tfsWs = hudsonWs.child("tfs-ws");
        tfsWs.mkdirs();
        tfsWs.createTempFile("temp", "txt");
        
        prepareCommonMocks();
        when(workspaces.exists(new Workspace("workspace"))).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, "tfs-ws", false, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
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
        
        prepareCommonMocks();
        when(workspaces.exists(new Workspace("workspace"))).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, "tfs-ws", false, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        assertTrue("The local folder was removed", tfsWs.exists());
        assertEquals("The local TFS folder was not cleaned", 0, tfsWs.list((FileFilter)null).size());
        assertEquals("The Hudson workspace path was cleaned", 2, hudsonWs.list((FileFilter)null).size());
    }

    @Test
    public void assertWorkspaceIsNotCleanedIfUsingUpdate() throws Exception {
        FilePath tfsWs = hudsonWs.child("tfs-ws");
        tfsWs.mkdirs();
        tfsWs.createTempFile("temp", "txt");
        
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, "tfs-ws", true, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));

        assertTrue("The local folder was removed", tfsWs.exists());
        assertEquals("The TFS workspace path was cleaned", 1, hudsonWs.list((FileFilter)null).size());
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutBySingleVersionSpecDeletesWorkspaceAtStartIfNotUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(project.getProjectPath()).thenReturn("project");
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verify(workspaces).deleteWorkspace(workspace);
        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(workspaces).getWorkspaceMapping(anyString());
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDeletesWorkspaceAtStartIfNotUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(project.getProjectPath()).thenReturn("project");
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verify(workspaces).deleteWorkspace(workspace);
        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(workspaces).getWorkspaceMapping(anyString());
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDoesNotDeleteWorkspaceAtStartIfUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verify(workspaces).getWorkspaceMapping(anyString());
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutBySingleVersionSpecDoesNotDeleteWorkspaceAtStartIfUsingUpdate() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verify(workspaces).getWorkspaceMapping(anyString());
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDoesNotDeleteWorkspaceIfNotUsingUpdateAndThereIsNoWorkspace() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(false).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        when(project.getProjectPath()).thenReturn("project");

        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkout(server, hudsonWs, null, Util.getCalendar(2009, 9, 24));
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(workspaces).getWorkspaceMapping(anyString());
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutBySingleVersionSpecDoesNotDeleteWorkspaceIfNotUsingUpdateAndThereIsNoWorkspace() throws Exception {
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(false).thenReturn(false);
        when(workspaces.newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class))).thenReturn(workspace);
        when(project.getProjectPath()).thenReturn("project");

        new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", false, false).checkoutBySingleVersionSpec(server, hudsonWs, MY_LABEL);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).newWorkspace(eq("workspace"), eq("project"), eq(EMPTY_CLOAKED_PATHS_LIST), eq(EMPTY_MAPPED_PATHS_MAP), isA(String.class));
        verify(workspaces).getWorkspaceMapping(anyString());
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(6596)
    @Test
    public void assertCheckoutOnlyRetrievesChangesToTheStartTimestampForCurrentBuild() throws Exception {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        prepareCommonMocks();
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getDetailedHistoryWithoutCloakedPaths(isA(VersionSpec.class), isA(VersionSpec.class), anyCollection(), anyCollection())).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", EMPTY_CLOAKED_PATHS_LIST, EMPTY_MAPPED_PATHS_MAP, ".", true, false);
        final Calendar startDate = Util.getCalendar(2008, 9, 24);
        final Calendar endDate = Util.getCalendar(2009, 9, 24);
        List<ChangeSet> actualList = action.checkout(server, hudsonWs, startDate, endDate);
        assertEquals("The list from the detailed history, was not the same as returned from checkout", list, actualList);

        final DateVersionSpec startDateVersionSpec = new DateVersionSpec(startDate);
        final DateVersionSpec endDateVersionSpec = new DateVersionSpec(endDate);
        verify(project).getDetailedHistoryWithoutCloakedPaths(
                argThat(new DateVersionSpecMatcher(startDateVersionSpec)),
                argThat(new DateVersionSpecMatcher(endDateVersionSpec)),
                eq(EMPTY_CLOAKED_PATHS_LIST),
                eq(EMPTY_MAPPED_PATHS_LIST));
        verify(project).getFiles(isA(String.class), eq("D2009-09-24T00:00:00Z"), eq(false));
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
