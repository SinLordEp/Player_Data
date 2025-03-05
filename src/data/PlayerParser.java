package data;

import Interface.EntityParser;
import Interface.ParserCallBack;
import Interface.VerifiedEntity;
import data.file.FileType;
import data.file.xml_utils;
import data.http.PhpType;
import exceptions.DataCorruptedException;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.Player;
import model.Region;
import model.Server;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class PlayerParser implements EntityParser {
    public static PlayerParser instance;

    public static PlayerParser getInstance() {
        if (instance == null) {
            instance = new PlayerParser();
        }
        return instance;
    }

    @Override
    public ParserCallBack<?, TreeMap<Integer, VerifiedEntity>> parseAll(Object dataType){
        return switch (dataType){
            case DataSource.DATABASE -> (ParserCallBack<ResultSet, TreeMap<Integer, VerifiedEntity>>) this::parseResultSet;
            case DataSource.HIBERNATE, DataSource.OBJECTDB -> (ParserCallBack<List<VerifiedEntity>, TreeMap<Integer, VerifiedEntity>>) this::parseList;
            case DataSource.MONGO -> (ParserCallBack<Document, TreeMap<Integer, VerifiedEntity>>) this::parseMongoDocument;
            case DataSource.BASEX, FileType.XML -> (ParserCallBack<Element, TreeMap<Integer, VerifiedEntity>>) this::parseXmlElement;
            case PhpType.JSON -> (ParserCallBack<JSONObject, TreeMap<Integer, VerifiedEntity>>) this::parseJsonObject;
            case FileType.TXT -> (ParserCallBack<String, TreeMap<Integer, VerifiedEntity>>) this::parseStringLine;
            case FileType.DAT -> (ParserCallBack<VerifiedEntity, TreeMap<Integer, VerifiedEntity>>) this::parseVerifiedEntity;
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        };
    }

    @Override
    public ParserCallBack<?, VerifiedEntity> serializeOne(Object dataType) {
        return switch (dataType) {
            case DataSource.DATABASE -> (ParserCallBack<PreparedStatement, VerifiedEntity>) this::playerToUpdateStatement;
            case DataSource.HIBERNATE, DataSource.OBJECTDB -> null;
            case DataSource.MONGO -> (ParserCallBack<Document, VerifiedEntity>) this::playerToMongoDocument;
            case DataSource.BASEX -> (ParserCallBack<String[], VerifiedEntity>) this::playerToBaseXQuery;
            case PhpType.JSON -> (ParserCallBack<JSONObject, VerifiedEntity>) this::playerToJsonObject;
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        };
    }

    @Override
    public ParserCallBack<?, TreeMap<Integer, VerifiedEntity>> serializeAll(Object dataType){
        return switch (dataType) {
            case FileType.XML -> (ParserCallBack<org.w3c.dom.Document, TreeMap<Integer, VerifiedEntity>>) this::playerToXmlElement;
            case FileType.TXT -> (ParserCallBack<ArrayList<String>, TreeMap<Integer, VerifiedEntity>>) this::playerToArrayString;
            case FileType.DAT -> (ParserCallBack<ArrayList<Player>, TreeMap<Integer, VerifiedEntity>>) this::playerToArrayEntity;
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        };
    }
    private void parseResultSet(ResultSet resultSet, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        try {
            Player player = new Player();
            player.setID(resultSet.getInt("id"));
            player.setName(resultSet.getString("name"));
            player.setRegion(new Region(resultSet.getString("region")));
            player.setServer(new Server(resultSet.getString("server"), player.getRegion()));
            dataMap.put(player.getID(), player);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private void playerToUpdateStatement(PreparedStatement statement, DataOperation operation, VerifiedEntity verifiedEntity){
        try {
            Player player = (Player)  verifiedEntity;
            switch (operation){
                case ADD:
                    statement.setInt(1, player.getID());
                    statement.setString(2, player.getRegion().toString());
                    statement.setString(3, player.getServer().toString());
                    statement.setString(4, player.getName());
                    break;
                case MODIFY:
                    statement.setString(1, player.getRegion().toString());
                    statement.setString(2, player.getServer().toString());
                    statement.setString(3, player.getName());
                    statement.setInt(4, player.getID());
                    break;
                case DELETE:
                    statement.setInt(1, player.getID());
                    break;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private void parseList(List<VerifiedEntity> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        list.forEach(verifiedEntity -> parseVerifiedEntity(verifiedEntity, null, dataMap));
    }


    private void parseMongoDocument(Document document, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Player player = new Player();
        player.setID(document.getInteger("id"));
        player.setName(document.getString("name"));
        player.setRegion(new Region(document.getString("region")));
        player.setServer(new Server(document.getString("server"), player.getRegion()));
        dataMap.put(player.getID(), player);
    }

    private void playerToMongoDocument(Document document, DataOperation operation, VerifiedEntity verifiedEntity){
        Player player = (Player) verifiedEntity;
        switch(operation){
            case ADD, MODIFY: document.put("id", player.getID());
                document.put("name", player.getName());
                document.put("region", player.getRegion().getName());
                document.put("server", player.getServer().getName());
                break;
            case DELETE: document.put("id", player.getID());
                break;
        }
    }

    private void parseStringLine(String line, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        String[] player_txt = line.split(";");
        Player player = new Player();
        player.setID(Integer.parseInt(player_txt[0]));
        player.setRegion(new Region(player_txt[1]));
        player.setServer(new Server(player_txt[2], player.getRegion()));
        player.setName(player_txt[3]);
        dataMap.put(player.getID(),player);
    }

    private void playerToArrayString(ArrayList<String> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        dataMap.values().forEach(verifiedEntity -> {
            Player player = (Player) verifiedEntity;
            list.add("%s;%s;%s;%s".formatted(player.getID(),player.getRegion(),player.getServer(),player.getName()));
        });
    }

    private void parseVerifiedEntity(VerifiedEntity entity, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Player player = (Player) entity;
        dataMap.put(player.getID(),player);
    }

    private void playerToArrayEntity(ArrayList<Player> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        dataMap.values().forEach(verifiedEntity -> {
            Player player = (Player) verifiedEntity;
            list.add(player);
        });
    }

    private void parseXmlElement(Element element, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        NodeList playerNodes = element.getElementsByTagName("player");
        for (int i = 0; i < playerNodes.getLength(); i++) {
            Node playerNode = playerNodes.item(i);
            if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element playerElement = (Element) playerNode;
                Player player = new Player();
                player.setID(Integer.parseInt(playerElement.getAttribute("id")));
                player.setRegion(new Region(xml_utils.getElementTextContent(playerElement, "region")));
                player.setServer(new Server(xml_utils.getElementTextContent(playerElement, "server"), player.getRegion()));
                player.setName(xml_utils.getElementTextContent(playerElement, "name"));
                dataMap.put(player.getID(), player);
            }
        }
    }

    private void playerToXmlElement(org.w3c.dom.Document document, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Element rootElement = document.createElement("Player");
        document.appendChild(rootElement);
        dataMap.values().forEach(verifiedEntity -> {
            Player player = (Player) verifiedEntity;
            Element playerElement = document.createElement("player");
            playerElement.setAttribute("id", String.valueOf(player.getID()));
            xml_utils.createElementWithText(document, playerElement, "region", player.getRegion().toString());
            xml_utils.createElementWithText(document, playerElement, "server", player.getServer().toString());
            xml_utils.createElementWithText(document, playerElement, "name", player.getName());
            rootElement.appendChild(playerElement);
        });
    }

    private void playerToBaseXQuery(String[] query, DataOperation operation, VerifiedEntity verifiedEntity){
        Player player = (Player) verifiedEntity;
        query[0] = switch (operation){
            case ADD -> "insert node <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player> into /Player"
                    .formatted(player.getID(), player.getRegion(), player.getServer(), player.getName());
            case MODIFY -> "replace node /Player/player[@id='%s'] with <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player>"
                    .formatted(player.getID(), player.getID(), player.getRegion(), player.getServer(), player.getName());
            case DELETE -> "delete node /Player/player[@id='%s']".formatted(player.getID());
            default -> throw new OperationException("Invalid DataOperation");
        };
    }

    private void parseJsonObject(JSONObject jsonObject, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        JSONArray playersArray = (JSONArray) jsonObject.get("players");
        if(playersArray.isEmpty()) {
            throw new DataCorruptedException("No players found");
        }
        for (Object object : playersArray) {
            Player player = new Player();
            JSONObject playerObject = (JSONObject) object;
            player.setID(Integer.parseInt(playerObject.get("id").toString()));
            player.setName(playerObject.get("name").toString());
            player.setRegion(new Region(playerObject.get("region").toString()));
            player.setServer(new Server(playerObject.get("server").toString(), player.getRegion()));
            dataMap.put(player.getID(), player);
        }
    }

    @SuppressWarnings("unchecked")
    private void playerToJsonObject(JSONObject jsonObject, DataOperation operation, VerifiedEntity verifiedEntity){
        Player player = (Player) verifiedEntity;
        jsonObject.put("id", player.getID());
        jsonObject.put("name", player.getName());
        jsonObject.put("region", player.getRegion().toString());
        jsonObject.put("server", player.getServer().toString());
        jsonObject.put("operation", operation.toString());
    }

}
