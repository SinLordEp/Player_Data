package data.file;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Utility class for working with XML files and documents. Provides methods for reading, writing,
 * creating, and manipulating XML data structures.
 * @author SIN
 */
public class xml_utils {

    /**
     * Creates an XML element with the specified tag name and text content, and appends it
     * as a child to the provided parent element within the given {@code Document}.
     *
     * @param document the {@code Document} where the new element will be created
     * @param parent the {@code Element} to which the new element will be appended
     * @param tagName the name of the XML tag for the new element
     * @param textContent the text content to set for the newly created element
     */
    public static void createElementWithText(Document document, Element parent, String tagName, String textContent) {
        Element element = document.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }


    /**
     * Retrieves the text content of the first child element with the specified tag name
     * within the provided parent {@code Element}.
     *
     * @param element the parent {@code Element} from which to retrieve the child element's text content
     * @param tagName the name of the tag corresponding to the child element whose text content is to be retrieved
     * @return the text content of the first matching child element, or {@code null} if no matching child element exists
     */
    public static String getElementTextContent(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0 && nodeList.item(0).getNodeType() == Node.ELEMENT_NODE) {
            return nodeList.item(0).getTextContent();
        }
        return null;
    }

    public static String nodeToString(Node node) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }
}
