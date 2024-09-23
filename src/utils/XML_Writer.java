package utils;

import model.FileOperationData;
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

public class XML_Writer implements File_Manager{

    public XML_Writer() {

    }

    public void update_Person(FileOperationData current_data) {
        Document document = create_document();
        if (document == null) {
            JOptionPane.showMessageDialog(null, "Error creating XML document.");
            return;
        }

        Element root = document.createElement("person");
        document.appendChild(root);
        switch (current_data.getPerson_type()) {
            case "Player":
                add_PlayerElements(document, root, current_data.getPerson_data());
                break;
            case "GM":
                break;
        }

        save_toFile(document, current_data.getPerson_file());
        JOptionPane.showMessageDialog(null, "New %s added".formatted(current_data.getPerson_type()));
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
            JOptionPane.showMessageDialog(null, "Error saving to file: " + e.getMessage());
        }
    }
}
