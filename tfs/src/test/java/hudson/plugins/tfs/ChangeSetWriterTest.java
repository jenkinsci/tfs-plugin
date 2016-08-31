package hudson.plugins.tfs;

import static org.custommonkey.xmlunit.XMLAssert.*;
import java.io.StringWriter;
import java.util.ArrayList;

import hudson.plugins.tfs.model.ChangeSet;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;

public class ChangeSetWriterTest {

    @Before
    public void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void assertWriterOutputsCorrectChangeLogXml() throws Exception {
        ChangeSet changeset = new ChangeSet("1122", Util.getCalendar(2008, 12, 12).getTime(), "rnd\\user", "comment");
        changeset.getItems().add(new ChangeSet.Item("path", "add"));
        changeset.getItems().add(new ChangeSet.Item("path2", "delete"));
        ArrayList<ChangeSet> sets = new ArrayList<ChangeSet>();
        sets.add(changeset);

        ChangeSetWriter changesetWriter = new ChangeSetWriter();
        StringWriter output = new StringWriter();
        changesetWriter.write(sets, output);
        assertXMLEqual("<?xml version=\"1.0\" encoding=\"UTF-8\"?><changelog>" +
                            "<changeset version=\"1122\">" +
                                "<date>2008-12-12T00:00:00Z</date>" +
                                "<user>rnd\\user</user>" +
                                "<comment>comment</comment>" +
                                "<items>" +
                                    "<item action=\"add\">path</item>" +
                                    "<item action=\"delete\">path2</item>" +
                                "</items>" +
                            "</changeset>" +
        		"</changelog>", output.getBuffer().toString());
    }

    @Test
    public void assertWriterIgnoredNullDomain() throws Exception {
        ChangeSet changeset = new ChangeSet("1122", Util.getCalendar(2008, 12, 12).getTime(), "user", "comment");
        ArrayList<ChangeSet> sets = new ArrayList<ChangeSet>();
        sets.add(changeset);

        ChangeSetWriter changesetWriter = new ChangeSetWriter();
        StringWriter output = new StringWriter();
        changesetWriter.write(sets, output);
        assertXMLEqual("<?xml version=\"1.0\" encoding=\"UTF-8\"?><changelog>" +
                            "<changeset version=\"1122\">" +
                                "<date>2008-12-12T00:00:00Z</date>" +
                                "<user>user</user>" +
                                "<comment>comment</comment>" +
                            "</changeset>" +
                        "</changelog>", output.getBuffer().toString());
    }

    @Test
    public void assertXmlCharsAreEscaped() throws Exception {
        ChangeSet changeset = new ChangeSet("1122", Util.getCalendar(2008, 12, 12).getTime(), "user", "Just <testing> \"what\" happens when I use the & character...Hudson does not seem to like it!");
        ArrayList<ChangeSet> sets = new ArrayList<ChangeSet>();
        sets.add(changeset);

        ChangeSetWriter changesetWriter = new ChangeSetWriter();
        StringWriter output = new StringWriter();
        changesetWriter.write(sets, output);
        assertXMLEqual("<?xml version=\"1.0\" encoding=\"UTF-8\"?><changelog>" +
                            "<changeset version=\"1122\">" +
                                "<date>2008-12-12T00:00:00Z</date>" +
                                "<user>user</user>" +
                                "<comment>Just &lt;testing&gt; &quot;what&quot; happens when I use the &amp; character...Hudson does not seem to like it!</comment>" +
                            "</changeset>" +
                        "</changelog>", output.getBuffer().toString());
    }

    @Test
    public void assertCheckedInByUserIsWritten() throws Exception {
        ChangeSet changeset = new ChangeSet("1122", Util.getCalendar(2008, 12, 12).getTime(), "user", "Just <testing> \"what\" happens when I use the & character...Hudson does not seem to like it!");
        changeset.setCheckedInBy("another_user");
        ArrayList<ChangeSet> sets = new ArrayList<ChangeSet>();
        sets.add(changeset);

        ChangeSetWriter changesetWriter = new ChangeSetWriter();
        StringWriter output = new StringWriter();
        changesetWriter.write(sets, output);
        assertXMLEqual("<?xml version=\"1.0\" encoding=\"UTF-8\"?><changelog>" +
                            "<changeset version=\"1122\">" +
                                "<date>2008-12-12T00:00:00Z</date>" +
                                "<user>user</user>" +
                                "<checked_in_by_user>another_user</checked_in_by_user>" +
                                "<comment>Just &lt;testing&gt; &quot;what&quot; happens when I use the &amp; character...Hudson does not seem to like it!</comment>" +
                            "</changeset>" +
                        "</changelog>", output.getBuffer().toString());
    }
}
