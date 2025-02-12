package data.file;

import GUI.Player.PlayerText;
import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;
import model.Player;
import model.Region;
import model.Server;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class XmlPlayerCRUD implements PlayerCRUD<DataInfo> {
    File file;
    String stringXML;
    boolean parseRawXML = false;
    DataInfo dataInfo;

    @Override
    public PlayerCRUD<DataInfo> prepare(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
        if(dataInfo.getUrl().startsWith("<Player>")){
            stringXML = dataInfo.getUrl();
            parseRawXML = true;
            return this;
        }else{
            file = new File(dataInfo.getUrl());
            if (file.exists() && file.canRead() && file.canWrite()){
                return this;
            }else{
                throw new FileManageException("File cannot be read or write");
            }
        }
    }



    @Override
    public void release() {
        file = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        Element root;
        try {
            if(parseRawXML){
                root = xml_utils.parseStringXml(stringXML);
            }else{
                root = xml_utils.readXml(file);
            }
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
        if (!dataInfo.getClassName().equals(root.getNodeName())) {
            throw new FileManageException("Invalid XML: Root element doesn't match XML");
        }
        if (!root.hasChildNodes()) {
            return this;
        }
        parser.parse((R)root, null, dataMap);
        return this;
    }

    @Override
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        return null;
    }

    public PlayerCRUD<String> read(ParserCallBack<String> data) {
        Element root;
        try {
            if(parseRawXML){
                root = xml_utils.parseStringXml(stringXML);
            }else{
                root = xml_utils.readXml(file);
            }
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
        if (!"Player".equals(root.getNodeName())) {
            throw new FileManageException("Invalid XML: Root element is not Player");
        }
        if (!root.hasChildNodes()) {
            PlayerText.getDialog().popup("player_map_null");
            return this;
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
                player_map.put(player.getID(), player);
            }
        }
        return this;
    }

    @Override
    public PlayerCRUD<String> update(HashMap<Player, DataOperation> changed_player_map) {
        return this;
    }

    /**
     * Writes the provided player data into an XML file at the specified file location.
     * The method uses {@code xml_utils.createDocument} to generate a new XML document,
     * {@code add_PlayerElements} to populate the XML with player data, and
     * {@code xml_utils.writeXml} to write the generated XML structure to the provided file.
     *
     * @param parser
     * @param dataMap a {@code TreeMap<Integer, Player>} containing player data to write. If the map
     *                is null, the resulting XML file will contain only the root element without any
     *                player data.
     * @throws FileManageException if an error occurs during XML creation, data population, or file writing.
     */
    @Override
    public PlayerCRUD<String> export(ParserCallBack<R> parser, TreeMap<Integer, VerifiedEntity> dataMap) {
        try {
            Document document = xml_utils.createDocument();
            Element root = document.createElement("Player");
            document.appendChild(root);
            if(dataMap != null){
                add_PlayerElements(document, root, dataMap);
            }
            xml_utils.writeXml(document, file);
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
        return this;
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
