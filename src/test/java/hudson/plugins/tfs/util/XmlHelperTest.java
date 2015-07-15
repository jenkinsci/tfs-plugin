package hudson.plugins.tfs.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class XmlHelperTest {

    @Test public void peekValue_Document() throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.newDocument();
        // <build>
        final Element root = doc.createElement("build");
            // <queueId>76</queueId>
            final Element queueIdNode = doc.createElement("queueId");
            queueIdNode.appendChild(doc.createTextNode("76"));
            root.appendChild(queueIdNode);

            // <timestamp>84</timestamp>
            final Element timestampNode = doc.createElement("timestamp");
            timestampNode.appendChild(doc.createTextNode("84"));
            root.appendChild(timestampNode);
        // </build>
        doc.appendChild(root);

        final String actualFound = XmlHelper.peekValue(doc, "/build/timestamp");

        Assert.assertEquals("84", actualFound);

        final String actualNotFound = XmlHelper.peekValue(doc, "/build/startTime");

        Assert.assertEquals(null, actualNotFound);
    }

    @Test public void peekValue_File() throws Exception {
        final Class<? extends XmlHelperTest> clazz = this.getClass();
        final String resourceBase = clazz.getSimpleName() + "/peekValue_File/";
        final URL inputUrl = clazz.getResource(resourceBase + "input.xml");
        File tmp = null;
        try {
            tmp = File.createTempFile("XmlHelperTest", "xml");
            FileUtils.copyURLToFile(inputUrl, tmp);

            final String actual = XmlHelper.peekValue(tmp, "/build/timestamp");

            Assert.assertEquals("1436542800239", actual);
        } finally {
            FileUtils.deleteQuietly(tmp);
        }
    }

    @Test public void pokeValue_Document() throws Exception {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final Document doc = db.newDocument();
        // <build>
        final Element root = doc.createElement("build");
            // <queueId>76</queueId>
            final Element queueIdNode = doc.createElement("queueId");
            queueIdNode.appendChild(doc.createTextNode("76"));
            root.appendChild(queueIdNode);

            // <timestamp>84</timestamp>
            final Element timestampNode = doc.createElement("timestamp");
            timestampNode.appendChild(doc.createTextNode("84"));
            root.appendChild(timestampNode);
        // </build>
        doc.appendChild(root);

        XmlHelper.pokeValue(doc, "/build/timestamp", "42");

        final XPathFactory xpf = XPathFactory.newInstance();
        final XPath xp = xpf.newXPath();
        final XPathExpression expression = xp.compile("/build/timestamp");
        final Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
        Assert.assertEquals("42", node.getTextContent());
    }

    @Test public void pokeValue_File() throws Exception {
        final Class<? extends XmlHelperTest> clazz = this.getClass();
        final String resourceBase = clazz.getSimpleName() + "/pokeValue_File/";
        final URL inputUrl = clazz.getResource(resourceBase + "input.xml");
        File tmp = null;
        BufferedReader expectedReader = null, actualReader = null;

        try {
            tmp = File.createTempFile("XmlHelperTest", "xml");
            FileUtils.copyURLToFile(inputUrl, tmp);

            XmlHelper.pokeValue(tmp, "/build/timestamp", "42");

            final URL expectedUrl = clazz.getResource(resourceBase + "expected.xml");
            expectedReader = new BufferedReader(new InputStreamReader(expectedUrl.openStream()));
            actualReader = new BufferedReader(new FileReader(tmp));
            assertReaders(expectedReader, actualReader);
        } finally {
            FileUtils.deleteQuietly(tmp);
            IOUtils.closeQuietly(expectedReader);
            IOUtils.closeQuietly(actualReader);
        }
    }

    /* Adapted from http://stackoverflow.com/a/466854/ */
    public static void assertReaders(final BufferedReader expected, final BufferedReader actual)
            throws IOException {
        String expectedLine;
        while ((expectedLine = expected.readLine()) != null) {
            final String actualLine = actual.readLine();
            Assert.assertNotNull("Expected had more lines than the actual.", actualLine);
            Assert.assertEquals(expectedLine, actualLine);
        }
        Assert.assertNull("Actual had more lines than the expected.", actual.readLine());
    }
}
