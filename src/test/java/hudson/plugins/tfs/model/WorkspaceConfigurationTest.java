package hudson.plugins.tfs.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class WorkspaceConfigurationTest {

    @Test public void assertConfigurationsEquals() {
    	ProjectData[] projects = new ProjectData[0];
    	
    	WorkspaceConfiguration one = new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("project", "workfolder", projects));
        WorkspaceConfiguration two = new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("project", "workfolder", projects));
        assertThat(one, is(two));
        assertThat(two, is(one));
        assertThat(one, is(one));
        assertThat(one, not(new WorkspaceConfiguration("aserver", "workspace", ProjectData.getProjects("project", "workfolder", projects))));
        assertThat(one, not(new WorkspaceConfiguration("server", "aworkspace", ProjectData.getProjects("project", "workfolder", projects))));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("aproject", "workfolder", projects))));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("project", "aworkfolder", projects))));
    }
}
