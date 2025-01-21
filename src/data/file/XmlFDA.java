package data.file;

import GUI.Player.PlayerText;
import Interface.PlayerFDA;
import exceptions.FileManageException;
import model.Player;
import model.Region;
import model.Server;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.TreeMap;

public class XmlFDA implements PlayerFDA {

    /**
     * Reads and parses an XML file containing player data, storing the information
     * in a {@code TreeMap} where the key is the player's ID and the value is the
     * corresponding {@code Player} object. The XML file must have a root element
     * named "Player" and child elements named "player" with required attributes and
     * child elements.
     * <p>
     * If the XML file does not meet the expected structure or an error occurs
     * during the reading process, exceptions are thrown or error handling
     * mechanisms are invoked.
     * <p>
     * This method utilizes {@code xml_utils.readXml} to parse the XML document and
     * {@code xml_utils.getElementTextContent} to retrieve element contents.
     * <p>
     * Displays a popup notification using {@code PlayerText.getDialog().popup}
     * when the XML document has no child nodes. Throws a {@code FileManageException}
     * if the root element is invalid or if there is an error during file handling.
     *
     * @param file The {@code File} object representing the XML file to be read.
     * @return A {@code TreeMap<Integer, Player>} where the key is the player ID and
     *         the value is the corresponding {@code Player} object. Returns an empty
     *         map if the file is empty or contains no player data.
     * @throws FileManageException if there is an error during XML reading or if the
     *                             root element is invalid.
     */
    @Override
    public TreeMap<Integer, Player> read(File file) {
        TreeMap<Integer, Player> player_data = new TreeMap<>();
        Element root;
        try {
            root = xml_utils.readXml(file);
        } catch (Exception e) {
            throw new FileManageException("Failed to read xml file. Cause: " + e.getMessage());
        }
        if (!"Player".equals(root.getNodeName())) {
            throw new FileManageException("Invalid XML: Root element is not Player");
        }
        if (!root.hasChildNodes()) {
            PlayerText.getDialog().popup("player_map_null");
            return player_data;
        }
        NodeList playerNodes = root.getElementsByTagName("player");
        for (int i = 0; i < playerNodes.getLength(); i++) {
            Node playerNode = playerNodes.item(i);
            if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element playerElement = (Element) playerNode;
                Player player = new Player();
                player.setID(Integer.parseInt(playerElement.getAttribute("id")));
                player.setRegion(new Region(xml_utils.getElementTextContent(playerElement, "region")));
                player.setServer(new Server(xml_utils.getElementTextContent(playerElement, "server"), player.getRegion()));
                player.setName(xml_utils.getElementTextContent(playerElement, "name"));
                player_data.put(player.getID(), player);
            }
        }
        return player_data;
    }

    /**
     * Writes the provided player data into an XML file at the specified file location.
     * The method uses {@code xml_utils.createDocument} to generate a new XML document,
     * {@code add_PlayerElements} to populate the XML with player data, and
     * {@code xml_utils.writeXml} to write the generated XML structure to the provided file.
     *
     * @param file the file where the player data will be written. If the file already exists,
     *                    it will be overwritten.
     * @param player_map a {@code TreeMap<Integer, Player>} containing player data to write. If the map
     *                    is null, the resulting XML file will contain only the root element without any
     *                    player data.
     * @throws FileManageException if an error occurs during XML creation, data population, or file writing.
     */
    @Override
    public void write(File file, TreeMap<Integer, Player> player_map) {
        try {
            Document document = xml_utils.createDocument();
            Element root = document.createElement("Player");
            document.appendChild(root);
            if(player_map != null){
                add_PlayerElements(document, root, player_map);
            }
            xml_utils.writeXml(document, file);
        } catch (Exception e) {
            throw new FileManageException("Failed to write player data via XML. Cause: " + e.getMessage());
        }
    }

    /**
     * Populates the given XML document with player data by creating and appending player elements
     * as child nodes of the specified root element. Each player element includes attributes
     * and child nodes representing player details such as ID, region, server, and name.
     * <p>
     * The method utilizes {@code xml_utils.createElementWithText} to help create child nodes
     * with text content for each player's properties.
     *
     * @param document the XML document where player data will be added. This document is expected
     *                 to be initialized prior to invoking the method.
     * @param root the root XML element to which the player elements will be appended.
     * @param player_data a {@code TreeMap<Integer, Player>} containing player information. Each
     *                    entry's key represents a unique player ID, and the value is a {@code Player}
     *                    object which contains the details to be inserted into the XML.
     */
    private void add_PlayerElements(Document document, Element root, TreeMap<Integer, Player> player_data) {
        for (Player player : player_data.values()) {
            Element playerElement = document.createElement("player");
            playerElement.setAttribute("id", String.valueOf(player.getID()));
            xml_utils.createElementWithText(document, playerElement, "region", player.getRegion().toString());
            xml_utils.createElementWithText(document, playerElement, "server", player.getServer().toString());
            xml_utils.createElementWithText(document, playerElement, "name", player.getName());
            root.appendChild(playerElement);
        }
    }
}
