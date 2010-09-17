package hudson.plugins.tfs.actions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hudson.FilePath;
import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CheckoutActionTest {

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
    public void assertFirstCheckoutNotUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", false).checkout(server, hudsonWs,null, Util.getCalendar(2009, 9, 24));
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".", "D2009-09-24T00:00:00Z");
        verify(workspaces).deleteWorkspace(workspace);
    }

    @Test
    public void assertFirstCheckoutUsingUpdate() throws Exception {
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists(new Workspace(server, "workspace"))).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        new CheckoutAction("workspace", "project", ".", true).checkout(server, hudsonWs,null, Util.getCalendar(2009, 9, 24));
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".", "D2009-09-24T00:00:00Z");
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

        verify(project).getFiles(".", "D2009-09-24T00:00:00Z");
        verify(workspaces, never()).newWorkspace("workspace");
        verify(workspace, never()).mapWorkfolder(project, ".");
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
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
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".", "D2009-09-24T00:00:00Z");
        verify(workspaces).deleteWorkspace(workspace);
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
    public void assertDetailedHistoryIsRetrievedInSecondBuild() throws Exception {
        List<ChangeSet> list = new ArrayList<ChangeSet>();
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getDetailedHistory(isA(Calendar.class), isA(Calendar.class))).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        List<ChangeSet> actualList = action.checkout(server, hudsonWs, Util.getCalendar(2008, 9, 24), Util.getCalendar(2008, 10, 24));
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        verify(project).getDetailedHistory(eq(Util.getCalendar(2008, 9, 24)), isA(Calendar.class));
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
        when(project.getDetailedHistory(isA(Calendar.class), isA(Calendar.class))).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        List<ChangeSet> actualList = action.checkout(server, hudsonWs, Util.getCalendar(2008, 9, 24), Util.getCalendar(2009, 9, 24));
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        verify(project).getDetailedHistory(eq(Util.getCalendar(2008, 9, 24)), eq(Util.getCalendar(2009, 9, 24)));
        verify(project).getFiles(".", "D2009-09-24T00:00:00Z");
    }
}
