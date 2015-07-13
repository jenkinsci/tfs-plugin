package hudson.plugins.tfs.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XmlHelper {

    private static final XPathFactory XPF = XPathFactory.newInstance();

    public static void pokeValue(final Document doc, final String xpathExpression, final String value) throws XPathExpressionException {
        final XPath xPath = XPF.newXPath();
        final XPathExpression expression = xPath.compile(xpathExpression);

        final Node node = (Node) expression.evaluate(doc, XPathConstants.NODE);
        // or setValue()?
        node.setTextContent(value);
    }
}
