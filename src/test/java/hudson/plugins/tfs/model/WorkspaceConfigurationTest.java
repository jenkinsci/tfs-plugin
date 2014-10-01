package hudson.plugins.tfs.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class WorkspaceConfigurationTest {

    @Test public void assertConfigurationsEquals() {
    	List<String> cloakList = new ArrayList<String>();
    	cloakList.add("cloak");
    	
        WorkspaceConfiguration one = new WorkspaceConfiguration("server", "workspace", "project", cloakList, "workfolder");
        WorkspaceConfiguration two = new WorkspaceConfiguration("server", "workspace", "project", cloakList, "workfolder");
        assertThat(one, is(two));
        assertThat(two, is(one));
        assertThat(one, is(one));
        assertThat(one, not(new WorkspaceConfiguration("aserver", "workspace", "project", cloakList, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "aworkspace", "project", cloakList, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "aproject", cloakList, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", cloakList, "aworkfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", new ArrayList<String>(), "aworkfolder")));
    }
}
