package hudson.plugins.tfs.util;

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

public class XmlHelperTest {

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

}
