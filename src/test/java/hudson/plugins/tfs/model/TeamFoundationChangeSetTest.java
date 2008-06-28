package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import hudson.plugins.tfs.model.TeamFoundationChangeSet.Item;

import org.junit.Test;


public class TeamFoundationChangeSetTest {

    @Test
    public void assertAffectedPathsReturnsPaths() {
        TeamFoundationChangeSet changeset = new TeamFoundationChangeSet("0", null, null, null);
        changeset.getItems().add(new Item("filename", "add"));
        changeset.getItems().add(new Item("filename2", "edit"));
        Collection<String> paths = changeset.getAffectedPaths();
        assertNotNull("Affected paths can not be null", paths);
        assertEquals("The number of affected paths was incorrect", 2, paths.size());
        Iterator<String> iterator = paths.iterator();
        assertEquals("The first path is incorrect", "filename", iterator.next());
        assertEquals("The first path is incorrect", "filename2", iterator.next());
    }
}
