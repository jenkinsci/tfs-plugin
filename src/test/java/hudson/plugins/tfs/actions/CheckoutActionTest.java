package hudson.plugins.tfs.actions;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import hudson.plugins.tfs.Util;
import hudson.plugins.tfs.model.ChangeSet;
import hudson.plugins.tfs.model.Project;
import hudson.plugins.tfs.model.Server;
import hudson.plugins.tfs.model.Workspace;
import hudson.plugins.tfs.model.Workspaces;

import org.junit.Test;


public class CheckoutActionTest {

    @Test
    public void assertFirstCheckoutNotUsingUpdate() throws Exception {
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        stub(server.getWorkspaces()).toReturn(workspaces);
        stub(server.getProject("project")).toReturn(project);
        stub(workspaces.exists(new Workspace(server, "workspace"))).toReturn(false);
        stub(workspaces.newWorkspace("workspace")).toReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", false);
        action.checkout(server, null);
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".");
        verify(workspaces).deleteWorkspace(workspace);
    }

    @Test
    public void assertFirstCheckoutUsingUpdate() throws Exception {
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        stub(server.getWorkspaces()).toReturn(workspaces);
        stub(server.getProject("project")).toReturn(project);
        stub(workspaces.exists(new Workspace(server, "workspace"))).toReturn(false);
        stub(workspaces.newWorkspace("workspace")).toReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        action.checkout(server, null);
        
        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".");
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertSecondCheckoutUsingUpdate() throws Exception {
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        stub(server.getWorkspaces()).toReturn(workspaces);
        stub(server.getProject("project")).toReturn(project);
        stub(workspaces.exists(new Workspace(server, "workspace"))).toReturn(true);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        action.checkout(server, null);

        verify(project).getFiles(".");
        verify(workspaces, never()).newWorkspace("workspace");
        verify(workspace, never()).mapWorkfolder(project, ".");
        verify(workspaces, never()).deleteWorkspace(isA(Workspace.class));
    }

    @Test
    public void assertSecondCheckoutNotUsingUpdate() throws Exception {
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Workspace workspace = mock(Workspace.class);
        Project project = mock(Project.class);

        stub(server.getWorkspaces()).toReturn(workspaces);
        stub(server.getProject("project")).toReturn(project);
        stub(workspaces.exists(new Workspace(server, "workspace"))).toReturn(false);
        stub(workspaces.newWorkspace("workspace")).toReturn(workspace);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", false);
        action.checkout(server, null);

        verify(workspaces).newWorkspace("workspace");
        verify(workspace).mapWorkfolder(project, ".");
        verify(project).getFiles(".");
        verify(workspaces).deleteWorkspace(isA(Workspace.class));
    }

//    @Test
//    public void assertWorkspaceIsDeletedIfItAlreadyExistsWhenNotUsingUpdate() throws Exception {
//        Server server = mock(Server.class);        
//        Workspaces workspaces = mock(Workspaces.class);
//        Workspace workspace = mock(Workspace.class);
//        Project project = mock(Project.class);
//
//        stub(server.getWorkspaces()).toReturn(workspaces);
//        stub(server.getProject("project")).toReturn(project);
//        stub(workspaces.exists(new Workspace(server, "workspace"))).toReturn(true);
//        stub(workspaces.newWorkspace("workspace")).toReturn(workspace);
//        
//        CheckoutAction action = new CheckoutAction("workspace", "project", ".", false);
//        action.checkout(server, null);
//
//        verify(workspaces).newWorkspace("workspace");
//        verify(workspace).mapWorkfolder(project, ".");
//        verify(project).getFiles(".");
//        verify(workspaces).deleteWorkspace(isA(Workspace.class));
//    }
    
    @Test
    public void assertDetailedHistoryIsNotRetrievedInFirstBuild() throws Exception {
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Project project = mock(Project.class);

        stub(server.getWorkspaces()).toReturn(workspaces);
        stub(server.getProject("project")).toReturn(project);
        stub(workspaces.exists(new Workspace(server, "workspace"))).toReturn(true);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        action.checkout(server, null);
        
        verify(project, never()).getDetailedHistory(isA(Calendar.class), isA(Calendar.class));
    }
    
    @Test
    public void assertDetailedHistoryIsRetrievedInSecondBuild() throws Exception {
        Server server = mock(Server.class);        
        Workspaces workspaces = mock(Workspaces.class);
        Project project = mock(Project.class);

        List<ChangeSet> list = new ArrayList<ChangeSet>();
        stub(server.getWorkspaces()).toReturn(workspaces);
        stub(server.getProject("project")).toReturn(project);
        stub(workspaces.exists(new Workspace(server, "workspace"))).toReturn(true);
        stub(project.getDetailedHistory(isA(Calendar.class), isA(Calendar.class))).toReturn(list);
        
        CheckoutAction action = new CheckoutAction("workspace", "project", ".", true);
        List<ChangeSet> actualList = action.checkout(server, Util.getCalendar(2008, 9, 24));
        assertSame("The list from the detailed history, was not the same as returned from checkout", list, actualList);
        
        verify(project).getDetailedHistory(eq(Util.getCalendar(2008, 9, 24)), isA(Calendar.class));
    }
}
