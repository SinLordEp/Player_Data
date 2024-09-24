package utils;

import model.GeneralOperationData;
import model.Player;
import model.PlayerOperationData;
import model.Person;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Map;

public class XMLWriter implements FileWriter{

    public XMLWriter(PlayerOperationData current_data) {

    }

    @Override
    public void write_player() {
        Document document = create_document();
        if (document == null) {
            JOptionPane.showMessageDialog(null, "Error creating XML document.");
            return;
        }

        Element root = document.createElement("person");
        document.appendChild(root);
        add_PlayerElements(document, root, player_data);

        save_toFile(document, current_data.getFile());
        JOptionPane.showMessageDialog(null, "New Player added");
    }

    private Document create_document() {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            return documentBuilder.newDocument();
        } catch (Exception e) {
            return null;
        }
    }


    private void add_PlayerElements(Document document, Element root, Map<Integer, Person> person_data) {
        for (Map.Entry<Integer, Person> entry : person_data.entrySet()) {
            Person player = entry.getValue();
            Element playerElement = create_PlayerElement(document, player);
            root.appendChild(playerElement);
        }
    }

    private Element create_PlayerElement(Document document, Person player) {
        Element playerElement = document.createElement("player");
        playerElement.setAttribute("id", String.valueOf(player.getID()));
        create_ElementText(document, playerElement, "region", player.getRegion());
        create_ElementText(document, playerElement, "server", player.getServer());
        create_ElementText(document, playerElement, "name", player.getName());
        return playerElement;
    }

    private void create_ElementText(Document document, Element parent, String tagName, String textContent) {
        Element element = document.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
    }

    private void save_toFile(Document document, File file) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

}
