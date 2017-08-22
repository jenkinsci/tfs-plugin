//CHECKSTYLE:OFF
package hudson.plugins.tfs.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class XmlHelper {

    private static final XPathFactory XPF = XPathFactory.newInstance();
    private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory TF = TransformerFactory.newInstance();

    public static String peekValue(final Document doc, final String xpathExpression)
            throws XPathExpressionException {
        final XPath xPath = XPF.newXPath();
        final XPathExpression expression = xPath.compile(xpathExpression);

        final Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
        final String result = (node != null) ? node.getTextContent() : null;
        return result;
    }

    public static String peekValue(final File file, final String xpathExpression)
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        final DocumentBuilder db = DBF.newDocumentBuilder();
        final FileInputStream fis = new FileInputStream(file);
        final Document doc;
        try {
            doc = db.parse(fis);
        }
        finally {
            fis.close();
        }

        final String result = peekValue(doc, xpathExpression);
        return result;
    }

    public static void pokeValue(final Document doc, final String xpathExpression, final String value) throws XPathExpressionException {
        final XPath xPath = XPF.newXPath();
        final XPathExpression expression = xPath.compile(xpathExpression);

        final Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
        // or setValue()?
        node.setTextContent(value);
    }

    public static void pokeValue(final File file, final String xpathExpression, final String value)
            throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, TransformerException {
        // TODO: it might be better performance to do a SAX read/write
        final DocumentBuilder db = DBF.newDocumentBuilder();
        final FileInputStream fis = new FileInputStream(file);
        final Document doc;
        try {
            doc = db.parse(fis);
        }
        finally {
            fis.close();
        }

        pokeValue(doc, xpathExpression, value);

        final Transformer t = TF.newTransformer();
        final DOMSource source = new DOMSource(doc);
        final FileOutputStream fos = new FileOutputStream(file);
        try {
            final StreamResult result = new StreamResult(fos);
            t.transform(source, result);
        }
        finally {
            fos.close();
        }
    }
}
