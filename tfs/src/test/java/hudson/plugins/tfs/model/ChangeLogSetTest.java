package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class ChangeLogSetTest {

    @Test
    public void assertChangeSetsHaveLogSetParent() throws Exception {        
        List<ChangeSet> changesets = new ArrayList<ChangeSet>();
        changesets.add(new ChangeSet("version", null, "user", "comment"));        
        ChangeLogSet logset = new ChangeLogSet(null, null, changesets);
        ChangeSet changeset = logset.iterator().next();
        assertNotNull("Log set parent was null change set", changeset.getParent());
    }

    @Test
    public void assertIsEmptyReturnsFalseWhenNoChangesets() throws Exception {        
        List<ChangeSet> changesets = new ArrayList<ChangeSet>();    
        ChangeLogSet logset = new ChangeLogSet(null, null, changesets);
        assertTrue("The isEmpty did not return true with an empty log set", logset.isEmptySet());
    }

    @Test
    public void assertIsEmptyReturnsTrueWithChangesets() throws Exception {        
        List<ChangeSet> changesets = new ArrayList<ChangeSet>();
        changesets.add(new ChangeSet("version", null, "user", "comment"));        
        ChangeLogSet logset = new ChangeLogSet(null, null, changesets);
        assertFalse("The isEmpty did not return false with a log set with change sets", logset.isEmptySet());
    }
}
