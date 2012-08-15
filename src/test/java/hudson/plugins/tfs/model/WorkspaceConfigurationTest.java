package hudson.plugins.tfs.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class WorkspaceConfigurationTest {

    @Test public void assertConfigurationsEquals() {
        WorkspaceConfiguration one = new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("project", "workfolder", null));
        WorkspaceConfiguration two = new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("project", "workfolder", null));
        assertThat(one, is(two));
        assertThat(two, is(one));
        assertThat(one, is(one));
        assertThat(one, not(new WorkspaceConfiguration("aserver", "workspace", ProjectData.getProjects("project", "workfolder", null))));
        assertThat(one, not(new WorkspaceConfiguration("server", "aworkspace", ProjectData.getProjects("project", "workfolder", null))));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("aproject", "workfolder", null))));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", ProjectData.getProjects("project", "aworkfolder", null))));
    }
}
