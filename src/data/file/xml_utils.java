package data.file;

import exceptions.FileManageException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;

/**
 * Utility class for working with XML files and documents. Provides methods for reading, writing,
 * creating, and manipulating XML data structures.
 * @author SIN
 */
public class xml_utils {

    /**
     * Reads and parses an XML file into an {@code Element} object representing the root element of the document.
     *
     * @param file the XML file to be read and parsed
     * @return the root element of the parsed XML document
     * @throws Exception if an error occurs while reading or parsing the XML file, such as file not found or invalid XML structure
     */
    public static Element readXml(File file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file);
        document.getDocumentElement().normalize();
        return document.getDocumentElement();
    }

    public static Element parseStringXml(String rawXML) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(rawXML.getBytes()));
        document.getDocumentElement().normalize();
        return document.getDocumentElement();
    }

    /**
     * Writes the given {@code Document} to the specified file as an XML file. The method ensures
     * the output is formatted with indentation for better readability. In case of errors during
     * the transformation or file writing process, it throws a {@code FileManageException}.
     *
     * @param document the {@code Document} object to be written to the XML file
     * @param file the {@code File} object representing the target XML file
     * @throws FileManageException if an error occurs during the XML writing process, such as
     *                              transformation or file output errors
     */
    public static void writeXml(Document document, File file) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
    }

    /**
     * Creates a new, empty {@code Document} object that can be used for building
     * or manipulating XML structures programmatically. This method utilizes
     * {@code DocumentBuilderFactory} and {@code DocumentBuilder} to instantiate the
     * {@code Document} object. In case of failure during the document creation process,
     * it throws a {@code FileManageException}.
     *
     * @return a new instance of {@code Document} representing an empty XML document
     * @throws FileManageException if the document creation process fails due to
     *                              configuration errors or internal exceptions
     */
    public static Document createDocument() {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
    }

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
