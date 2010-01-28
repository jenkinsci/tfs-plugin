package hudson.plugins.tfs.util;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;
import org.jvnet.hudson.test.Bug;


public class TextTableParserTest {

    @Test public void assertThatReaderWithoutTableIsParsed() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader("Just some text to be ignored"));
        assertFalse("There should not be any row", listParser.nextRow());
    }
    
    @Test public void assertColumnCount() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader("Just some text to be ignored\n" +
                "----- -- ------\n"));
        assertEquals("The column count was incorrect", 3, listParser.getColumnCount());
    }
    
    @Bug(4666)
    @Test public void assertDashInTextIsIgnored() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader("Server: server-name\n" +
                "----- -- ------\n"));
        assertEquals("The column count was incorrect", 3, listParser.getColumnCount());
    }

    @Test public void assertGetColumn() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader("Just some text to be ignored\n" +
                "----- -- ------\n" +
                "AAAAA BB CCCCCC"));
        listParser.nextRow();
        assertEquals("The column one was incorrect", "AAAAA", listParser.getColumn(0));
        assertEquals("The column two was incorrect", "BB", listParser.getColumn(1));
        assertEquals("The column three was incorrect", "CCCCCC", listParser.getColumn(2));
    }

    @Test public void assertNextRow() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader("Just some text to be ignored\n" +
                "----- -- ------\n" +
                "AAAAA BB CCCCCC\n" + 
                "LLLLL DD ZZZZZZ"));
        assertTrue("The nextLine() returned false", listParser.nextRow());
        assertTrue("The nextLine() returned false", listParser.nextRow());
        assertFalse("The nextLine() returned true", listParser.nextRow());
    }

    @Test public void assertNextRowWithNonsenseLine() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader("Just some text to be ignored\n" +
                "----- -- ------\n" +
                "\n" +
                "AAAAA BB CCCCCC" +
                "\n" +
                "AAAAA BB CCCCCC"));
        assertTrue("The nextLine() returned false", listParser.nextRow());
        assertEquals("The column one was incorrect", "AAAAA", listParser.getColumn(0));
        assertTrue("The nextLine() returned false", listParser.nextRow());
        assertFalse("The nextLine() returned true", listParser.nextRow());
    }

    @Test public void assertGetColumnWorksIfLastcolumnIsShorterThanLastColumnLength() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader("Just some text to be ignored\n" +
                "----- -- ------\n" +
                "AAAAA BB CCC"));
        listParser.nextRow();
        assertEquals("The column one was incorrect", "AAAAA", listParser.getColumn(0));
        assertEquals("The column two was incorrect", "BB", listParser.getColumn(1));
        assertEquals("The column three was incorrect", "CCC", listParser.getColumn(2));
    }

    @Test public void assertGetColumnWorksIfColumnIsShorter() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader(
                "----- -- ------\n" +
                "AA    BB CCCCCC"));
        listParser.nextRow();
        assertEquals("The column one was incorrect", "AA", listParser.getColumn(0));
    }

    @Test public void assertTableStartsImmediately() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader(
                "----- -- ------\n" +
                "AAAAA BB CCCCCC"));
        listParser.nextRow();
        assertEquals("The column one was incorrect", "AAAAA", listParser.getColumn(0));
        assertEquals("The column two was incorrect", "BB", listParser.getColumn(1));
        assertEquals("The column three was incorrect", "CCCCCC", listParser.getColumn(2));
    }

    @Test public void assertNextRowWorksWithOptionalColumns() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader(
                "----- -- ------\n" +
                "AAAAA BB\n" +
                "AAAAA BB DDDDDD"), 1);
        listParser.nextRow();
        assertEquals("The column one was incorrect", "AAAAA", listParser.getColumn(0));
        assertEquals("The column two was incorrect", "BB", listParser.getColumn(1));
        assertNull("The column three was incorrect", listParser.getColumn(2));
        listParser.nextRow();
        assertEquals("The column three was incorrect", "DDDDDD", listParser.getColumn(2));
    }

    @Test(expected=IllegalStateException.class) public void assertGetColumnAfterLastRowThrowsException() throws Exception {
        TextTableParser listParser = new TextTableParser(new StringReader(
                "----- -- ------\n" +
                "AAAAA BB"));
        listParser.nextRow();
        listParser.nextRow();
        listParser.getColumn(0);
    }
}
