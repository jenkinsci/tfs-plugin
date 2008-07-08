package hudson.plugins.tfs.model;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;

import hudson.plugins.tfs.model.ChangeSet.Item;
import hudson.scm.EditType;

import org.junit.Test;


public class ChangeSetTest {

    @Test
    public void assertMsgReturnsComment() {
        ChangeSet changeset = new ChangeSet("0", null, "snd\\user", "comment");
        assertSame("The getMsg() did not return the comment", "comment", changeset.getMsg());
    }
    
    @Test
    public void assertAffectedPathsReturnsPaths() {
        ChangeSet changeset = new ChangeSet("0", null, "snd\\user", "comment");
        changeset.getItems().add(new Item("filename", "add"));
        changeset.getItems().add(new Item("filename2", "edit"));
        Collection<String> paths = changeset.getAffectedPaths();
        assertNotNull("Affected paths can not be null", paths);
        assertEquals("The number of affected paths was incorrect", 2, paths.size());
        Iterator<String> iterator = paths.iterator();
        assertEquals("The first path is incorrect", "filename", iterator.next());
        assertEquals("The first path is incorrect", "filename2", iterator.next());
    }
    
    @Test
    public void assertAddedItemReturnsAddEditType() {
        Item item = new Item("path", "add");
        assertSame("Incorrect edit type returned for Add action", EditType.ADD, item.getEditType());
    }
    
    @Test
    public void assertDeletedItemReturnsDeleteEditType() {
        Item item = new Item("path", "delete");
        assertSame("Incorrect edit type returned for Delete action", EditType.DELETE, item.getEditType());
    }
    
    @Test
    public void assertModifiedItemReturnsEditEditType() {
        Item item = new Item("path", "edit");
        assertSame("Incorrect edit type returned for Edit action", EditType.EDIT, item.getEditType());
    }
    
    @Test
    public void assertUserNameIsSetCorrectly() {
        ChangeSet changeset = new ChangeSet("0", null, "RNO\\_MCLWEB", "comment");
        assertEquals("The user name was incorrect", "_MCLWEB", changeset.getUser());
    }
    
    @Test
    public void assertDomainNameIsSetCorrectly() {
        ChangeSet changeset = new ChangeSet("0", null, "RNO\\_MCLWEB", "comment");
        assertEquals("The domain name was incorrect", "RNO", changeset.getDomain());
    }
}
