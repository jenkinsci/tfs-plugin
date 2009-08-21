package hudson.plugins.tfs.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class WorkspaceConfigurationTest {

    @Test public void assertConfigurationsEquals() {
        WorkspaceConfiguration one = new WorkspaceConfiguration("server", "workspace", "project", "workfolder");
        WorkspaceConfiguration two = new WorkspaceConfiguration("server", "workspace", "project", "workfolder");
        assertThat(one, is(two));
        assertThat(two, is(one));
        assertThat(one, is(one));
        assertThat(one, not(new WorkspaceConfiguration("aserver", "workspace", "project", "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "aworkspace", "project", "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "aproject", "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", "aworkfolder")));
    }
}
