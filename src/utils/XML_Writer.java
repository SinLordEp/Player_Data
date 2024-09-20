package utils;

import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XML_Writer implements File_Manager{
    private final XML_Reader reader;

    public XML_Writer(XML_Reader reader) {
        this.reader = reader;
    }

    public void addPlayerToXML(File file, Player newPlayer) {
        try {
            Document document = reader.file_reading(file);
            if (document == null) {
                System.out.println("Failed to read the XML file.");
                return;
            }

            Node root = document.getDocumentElement();
            Element playerElement = createPlayerElement(document, newPlayer);
            root.appendChild(playerElement);

            saveDocumentToFile(document, file);
            JOptionPane.showMessageDialog(null,"New player added");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,e.getMessage());
        }
    }

    private Element createPlayerElement(Document document, Player player) {
        Element playerElement = document.createElement("player");
        playerElement.setAttribute("id", String.valueOf(player.getID()));
        addElementWithText(document, playerElement, "region", player.getRegion());
        addElementWithText(document, playerElement, "server", player.getServer());
        addElementWithText(document, playerElement, "name", player.getName());
        return playerElement;
    }

    private void addElementWithText(Document document, Element parent, String tagName, String textContent) {
        Element element = document.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private void saveDocumentToFile(Document document, File file) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }
}
