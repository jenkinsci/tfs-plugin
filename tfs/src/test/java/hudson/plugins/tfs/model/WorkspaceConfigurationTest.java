package hudson.plugins.tfs.model;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

public class WorkspaceConfigurationTest {

    private static final List<String> EMPTY_CLOAKED_PATHS_LIST = Collections.emptyList();
    private static final Map<String, String> EMPTY_MAPPING_PATHS_LIST = new TreeMap<String, String>();

    @Test public void assertConfigurationsEquals() {
        final List<String> cloakList = Collections.singletonList("cloak");
        final Map<String, String> mappings = new TreeMap<String, String>();
        mappings.put("$/foo/", "foo");
        mappings.put("$/bar/", "bar");
        mappings.put("$/baz/", "baz");

        final Map<String, String> almostSimilarMappings = new TreeMap<String, String>();
        mappings.put("$/foo/", "foo/");
        mappings.put("$/bar/", "bar");
        mappings.put("$/baz/", "baz");

        final Map<String, String> nullMappings = new TreeMap<String, String>();
        mappings.put("$/foo/", null);
        mappings.put("$/bar/", null);
        mappings.put("$/baz/", null);

        WorkspaceConfiguration one = new WorkspaceConfiguration("server", "workspace", "project", cloakList, mappings, "workfolder");
        WorkspaceConfiguration two = new WorkspaceConfiguration("server", "workspace", "project", cloakList, mappings, "workfolder");
        WorkspaceConfiguration three = new WorkspaceConfiguration("server", "workspace", "project", cloakList, almostSimilarMappings, "workfolder");
        WorkspaceConfiguration four = new WorkspaceConfiguration("server", "workspace", "project", cloakList, nullMappings, "workfolder");
        
        assertThat(one, is(two));
        assertThat(two, is(one));
        assertThat(one, is(one));

        assertThat(one, not(three));
        assertThat(one, not(four));

        assertThat(three, is(three));
        assertThat(four, is(four));
        
        assertThat(one, not(new WorkspaceConfiguration("aserver", "workspace", "project", cloakList, mappings, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "aworkspace", "project", cloakList, mappings, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "aproject", cloakList, mappings, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", cloakList, mappings, "aworkfolder")));

        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", null, mappings, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", EMPTY_CLOAKED_PATHS_LIST, mappings, "workfolder")));

        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", cloakList, null, "workfolder")));
        assertThat(one, not(new WorkspaceConfiguration("server", "workspace", "project", cloakList, EMPTY_MAPPING_PATHS_LIST, "workfolder")));
    }
}
