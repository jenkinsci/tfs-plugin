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
import org.junit.Test;
import org.jvnet.hudson.test.Bug;

public class CheckoutActionTest {

    private FilePath hudsonWs;

    @After public void teardown() throws Exception {
        if (hudsonWs != null) {
            hudsonWs.deleteRecursive();
        }
    }
    
    @Test
    public void assertFirstCheckoutNotUsingUpdate() throws Exception {
        hudsonWs = Util.createTempFilePath();
        
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", false);
        action.checkout(server, hudsonWs,null);
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".");
        verify(workspaces).deleteWorkspace(workspace);
    }

    @Test
    public void assertFirstCheckoutUsingUpdate() throws Exception {
        hudsonWs = Util.createTempFilePath();
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists(new Workspace(server, "workspace"))).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        action.checkout(server, hudsonWs,null);
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".");
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertSecondCheckoutUsingUpdate() throws Exception {
        hudsonWs = Util.createTempFilePath();
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        action.checkout(server, hudsonWs, null);

        verify(project).getFiles(".");
        verify(workspaces, never()).newWorkspace("workspace");
        verify(workspace, never()).mapWorkfolder(project, ".");
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertSecondCheckoutNotUsingUpdate() throws Exception {
        hudsonWs = Util.createTempFilePath();
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", false);
        action.checkout(server, hudsonWs,null);

        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".");
        verify(workspaces).deleteWorkspace(workspace);
    }
   
    @Test
    public void assertDetailedHistoryIsNotRetrievedInFirstBuild() throws Exception {
        hudsonWs = Util.createTempFilePath();
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Project project = mock(Project.class);
        Workspace workspace = mock(Workspace.class);

        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        action.checkout(server, hudsonWs, null);
        
        verify(project, never()).getDetailedHistory(isA(Calendar.class), isA(Calendar.class));
    }
    
    @Test
    public void assertDetailedHistoryIsRetrievedInSecondBuild() throws Exception {
        hudsonWs = Util.createTempFilePath();
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Project project = mock(Project.class);
        Workspace workspace = mock(Workspace.class);

        List<ChangeSet> list = new ArrayList<ChangeSet>();
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        when(project.getDetailedHistory(isA(Calendar.class), isA(Calendar.class))).thenReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        List<ChangeSet> actualList = action.checkout(server, hudsonWs, Util.getCalendar(2008, 9, 24));
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        verify(project).getDetailedHistory(eq(Util.getCalendar(2008, 9, 24)), isA(Calendar.class));
    }
    
    @Test
    public void assertWorkFolderIsCleanedIfNotUsingUpdate() throws Exception {
        
        hudsonWs = Util.createTempFilePath();
        hudsonWs.createTempFile("temp", "txt");
        FilePath tfsWs = hudsonWs.child("tfs-ws");
        tfsWs.mkdirs();
        tfsWs.createTempFile("temp", "txt");
        
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists(new Workspace(server, "workspace"))).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", "tfs-ws", false);
        action.checkout(server, hudsonWs, null);
        
        assertTrue("The local folder was removed", tfsWs.exists());
        assertEquals("The local TFS folder was not cleaned", 0, tfsWs.list((FileFilter)null).size());
        assertEquals("The Hudson workspace path was cleaned", 2, hudsonWs.list((FileFilter)null).size());
    }

    @Test
    public void assertWorkspaceIsNotCleanedIfUsingUpdate() throws Exception {
        
        hudsonWs = Util.createTempFilePath();
        FilePath tfsWs = hudsonWs.child("tfs-ws");
        tfsWs.mkdirs();
        tfsWs.createTempFile("temp", "txt");
        
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Project project = mock(Project.class);
        Workspace workspace = mock(Workspace.class);

        when(server.getWorkspaces()).thenReturn(workspaces);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.exists("workspace")).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getLocalHostname()).thenReturn("LocalComputer");
        when(workspace.getComputer()).thenReturn("LocalComputer");
        
        CheckoutAction action = new CheckoutAction("workspace", "project", "tfs-ws", true);
        action.checkout(server, hudsonWs, null);

        assertTrue("The local folder was removed", tfsWs.exists());
        assertEquals("The TFS workspace path was cleaned", 1, hudsonWs.list((FileFilter)null).size());
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDeletesWorkspaceAtStartIfNotUsingUpdate() throws Exception {
        hudsonWs = Util.createTempFilePath();
        
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);
        
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(false);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", false);
        action.checkout(server, hudsonWs, null);
        
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
        hudsonWs = Util.createTempFilePath();
        
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);
        
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(true).thenReturn(true);
        when(workspaces.getWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        action.checkout(server, hudsonWs, null);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).getWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
    
    @Bug(3882)
    @Test
    public void assertCheckoutDoesNotDeleteWorkspaceIfNotUsingUpdateAndThereIsNoWorkspace() throws Exception {
        hudsonWs = Util.createTempFilePath();
        
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);
        
        when(server.getWorkspaces()).thenReturn(workspaces);
        when(workspaces.exists("workspace")).thenReturn(false).thenReturn(false);
        when(workspaces.newWorkspace("workspace")).thenReturn(workspace);
        when(server.getProject("project")).thenReturn(project);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", false);
        action.checkout(server, hudsonWs, null);
        
        verify(server).getWorkspaces();
        verify(workspaces, times(2)).exists("workspace");
        verify(workspaces).newWorkspace("workspace");
        verifyNoMoreInteractions(workspaces);
    }
}
