package hudson.plugins.tfs.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class WorkspaceConfigurationTest {

    private static final List<String> EMPTY_CLOAKED_PATHS_LIST = Collections.emptyList();

    @Test public void assertConfigurationsEquals() {
        final List<String> cloakList = Collections.singletonList("cloak");

        WorkspaceConfiguration one = new WorkspaceConfiguration("server", "workspace", "project", cloakList, "workfolder");
        WorkspaceConfiguration two = new WorkspaceConfiguration("server", "workspace", "project", cloakList, "workfolder");
        assertThat(one, is(two));
        assertThat(two, is(one));
        assertThat(one, is(one));
        assertThat(one, not(new WorkspaceConfiguration("aserver", "workspace", "project", cloakList, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "aworkspace", "project", cloakList, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "aproject", cloakList, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", cloakList, "aworkfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", EMPTY_CLOAKED_PATHS_LIST, "workfolder")));
    }
}
