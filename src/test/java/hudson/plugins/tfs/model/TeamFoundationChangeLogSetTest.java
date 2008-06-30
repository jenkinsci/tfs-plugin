package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;


public class TeamFoundationChangeLogSetTest {

    @Test
    public void assertChangeSetsHaveLogSetParent() throws Exception {        
        List<TeamFoundationChangeSet> changesets = new ArrayList<TeamFoundationChangeSet>();
        changesets.add(new TeamFoundationChangeSet("version", null, "user", "comment"));        
        TeamFoundationChangeLogSet logset = new TeamFoundationChangeLogSet(null, changesets);
        TeamFoundationChangeSet changeset = logset.iterator().next();
        assertNotNull("Log set parent was null change set", changeset.getParent());
    }

    @Test
    public void assertIsEmptyReturnsFalseWhenNoChangesets() throws Exception {        
        List<TeamFoundationChangeSet> changesets = new ArrayList<TeamFoundationChangeSet>();    
        TeamFoundationChangeLogSet logset = new TeamFoundationChangeLogSet(null, changesets);
        assertTrue("The isEmpty did not return true with an empty log set", logset.isEmptySet());
    }

    @Test
    public void assertIsEmptyReturnsTrueWithChangesets() throws Exception {        
        List<TeamFoundationChangeSet> changesets = new ArrayList<TeamFoundationChangeSet>();
        changesets.add(new TeamFoundationChangeSet("version", null, "user", "comment"));        
        TeamFoundationChangeLogSet logset = new TeamFoundationChangeLogSet(null, changesets);
        assertFalse("The isEmpty did not return false with a log set with change sets", logset.isEmptySet());
    }
}
