package data.database;

import GUI.Player.PlayerText;
import Interface.PlayerCRUD;
import data.DataOperation;
import data.file.xml_utils;
import exceptions.DatabaseException;
import model.DatabaseInfo;
import model.Player;
import model.Region;
import model.Server;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.List;
import org.basex.core.cmd.XQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static data.file.xml_utils.nodeToString;
import static data.file.xml_utils.parseStreamXml;
import static main.principal.getProperty;

/**
 * @author SIN
 */

public class BaseXPlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    private static final Logger logger = LoggerFactory.getLogger(BaseXPlayerCRUD.class);
    Context context = new Context();

    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) {
        try {
            String dbList = new List().execute(context);
            if (!dbList.contains(databaseInfo.getDatabase())) {
                String baseXPath = getProperty("baseXPath");
                new CreateDB(databaseInfo.getDatabase(), baseXPath).execute(context);
            }
            return this;
        } catch (BaseXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void release() {
        context.close();
    }

    @Override
    public TreeMap<Integer, Player> read() throws Exception {
        String query = "/Player";
        TreeMap<Integer, Player> playerMap = new TreeMap<>();
        Element root = parseStreamXml(new ByteArrayInputStream(new XQuery(query).execute(context).getBytes()));
        if (!root.hasChildNodes()) {
            PlayerText.getDialog().popup("player_map_null");
            return playerMap;
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
                playerMap.put(player.getID(), player);
            }
        }
        release();
        return playerMap;
    }

    @Override
    public void update(HashMap<Player, DataOperation> changed_player_map) {
        logger.info("Update BaseX: Updating database...");
        try {
            for(Map.Entry<Player, DataOperation> player_operation : changed_player_map.entrySet()) {
                switch (player_operation.getValue()) {
                    case ADD -> addPlayer(player_operation.getKey());
                    case MODIFY -> modifyPlayer(player_operation.getKey());
                    case DELETE -> deletePlayer(player_operation.getKey());
                }
            }
        } catch (Exception e) {
            throw new DatabaseException("Failed to update database with Cause: " + e.getMessage());
        }
    }

    private void deletePlayer(Player player) {
        try {
            String query = String.format("delete node doc('Player')/root/player[@id='%d']", player.getID());
            new XQuery(query).execute(context);
            logger.info("Deleted player with ID: {}", player.getID());
        } catch (Exception e) {
            throw new DatabaseException("Failed to delete player with ID: " + player.getID() + ". Cause: " + e.getMessage());
        }
    }

    private void modifyPlayer(Player player) {
        try {
            String updateRegionQuery = String.format(
                    "replace value of node doc('Player')/root/player[@id='%d']/region with '%s'",
                    player.getID(),
                    player.getRegion().toString()
            );
            new XQuery(updateRegionQuery).execute(context);

            String updateServerQuery = String.format(
                    "replace value of node doc('Player')/root/player[@id='%d']/server with '%s'",
                    player.getID(),
                    player.getServer().toString()
            );
            new XQuery(updateServerQuery).execute(context);

            String updateNameQuery = String.format(
                    "replace value of node doc('Player')/root/player[@id='%d']/name with '%s'",
                    player.getID(),
                    player.getName()
            );
            new XQuery(updateNameQuery).execute(context);
            logger.info("Modified player with ID: {} ", player.getID());
        } catch (Exception e) {
            throw new DatabaseException("Failed to modify player with ID: " + player.getID() + ". Cause: " + e.getMessage());
        }
    }

    private void addPlayer(Player player) {
        Document document = xml_utils.createDocument();
        Element playerElement = document.createElement("player");
        playerElement.setAttribute("id", String.valueOf(player.getID()));
        xml_utils.createElementWithText(document, playerElement, "region", player.getRegion().toString());
        xml_utils.createElementWithText(document, playerElement, "server", player.getServer().toString());
        xml_utils.createElementWithText(document, playerElement, "name", player.getName());
        try {
            String elementString = nodeToString(playerElement);
            String insertQuery = String.format(
                    "insert node %s into doc('Player')/root", elementString
            );
            new XQuery(insertQuery).execute(context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void export(TreeMap<Integer, Player> playerMap) {
        try {
            Document document = xml_utils.createDocument();
            Element rootElement = document.createElement("root");
            document.appendChild(rootElement);

            for (Player player : playerMap.values()) {
                Element playerElement = document.createElement("player");
                playerElement.setAttribute("id", String.valueOf(player.getID()));
                xml_utils.createElementWithText(document, playerElement, "region", player.getRegion().toString());
                xml_utils.createElementWithText(document, playerElement, "server", player.getServer().toString());
                xml_utils.createElementWithText(document, playerElement, "name", player.getName());
                rootElement.appendChild(playerElement);
            }
            String xmlString = xml_utils.nodeToString(rootElement);
            String replaceQuery = String.format("replace node doc('Player')/root with %s", xmlString);
            new XQuery(replaceQuery).execute(context);

            logger.info("Export: Success");
        } catch (Exception e) {
            throw new DatabaseException("Failed to export Player data to database. Cause: " + e.getMessage());
        }
    }

}
