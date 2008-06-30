package hudson.plugins.tfs;

import static org.junit.Assert.*;
import hudson.plugins.tfs.model.TeamFoundationChangeLogSet;
import hudson.plugins.tfs.model.TeamFoundationChangeSet;
import hudson.plugins.tfs.model.TeamFoundationChangeSet.Item;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

public class ChangeSetReaderTest {

    @Test
    public void assertParsingTwoXmlChangeSets() throws Exception {
        Reader reader = new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?><changelog>" +
                            "<changeset version=\"1122\">" +
                                "<date>2009-01-12T00:00:00Z</date>" +
                                "<user>user</user>" +
                                "<comment>comment</comment>" +
                                "<items>" +
                                    "<item action=\"add\">path</item>" +
                                    "<item action=\"delete\">path2</item>" +
                                "</items>" +
                            "</changeset>" +
                        "</changelog>");
        
        ChangeSetReader changesetReader = new ChangeSetReader();
        TeamFoundationChangeLogSet logset = changesetReader.parse(null, reader);
        
        TeamFoundationChangeSet changeset = logset.iterator().next();
        assertEquals("User is incorrect", "user", changeset.getUser());
        assertEquals("Comment is incorrect", "comment", changeset.getComment());
        assertEquals("Version is incorrect", "1122", changeset.getVersion());
        assertEquals("Date is incorrect", Util.getCalendar(2009, 1, 12).getTime(), changeset.getDate());
        
        assertEquals("Number of items in change set was incorrect", 2, changeset.getItems().size());
        Item item = changeset.getItems().get(0);
        assertEquals("Action is incorrect", "add", item.getAction());
        assertEquals("Path is incorrect", "path", item.getPath());
        
        item = changeset.getItems().get(1);
        assertEquals("Action is incorrect", "delete", item.getAction());
        assertEquals("Path is incorrect", "path2", item.getPath());
    }
}
