package data;

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
public class PlayerParser {

    public static ParserCallBack<?, TreeMap<Integer, VerifiedEntity>> input(Object dataType){
        return switch (dataType){
            case DataSource.DATABASE -> (ParserCallBack<ResultSet, TreeMap<Integer, VerifiedEntity>>) PlayerParser::parseResultSet;
            case DataSource.HIBERNATE, DataSource.OBJECTDB -> (ParserCallBack<List<VerifiedEntity>, TreeMap<Integer, VerifiedEntity>>) PlayerParser::parseList;
            case DataSource.MONGO -> (ParserCallBack<Document, TreeMap<Integer, VerifiedEntity>>) PlayerParser::parseMongoDocument;
            case DataSource.BASEX, FileType.XML -> (ParserCallBack<Element, TreeMap<Integer, VerifiedEntity>>) PlayerParser::parseXmlElement;
            case PhpType.JSON -> (ParserCallBack<JSONObject, TreeMap<Integer, VerifiedEntity>>) PlayerParser::parseJsonObject;
            case FileType.TXT -> (ParserCallBack<ArrayList<String>, TreeMap<Integer, VerifiedEntity>>) PlayerParser::parseArrayListText;
            case FileType.DAT -> (ParserCallBack<VerifiedEntity, TreeMap<Integer, VerifiedEntity>>) PlayerParser::parseVerifiedEntity;
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        };
    }

    public static ParserCallBack<?, Player> singleOutput(Object dataType) {
        return switch (dataType) {
            case DataSource.DATABASE -> (ParserCallBack<PreparedStatement, Player>) PlayerParser::playerToUpdateStatement;
            case DataSource.HIBERNATE, DataSource.OBJECTDB -> null;
            case DataSource.MONGO -> (ParserCallBack<Document, Player>) PlayerParser::playerToMongoDocument;
            case DataSource.BASEX -> (ParserCallBack<String[], Player>) PlayerParser::playerToBaseXQuery;
            case PhpType.JSON -> (ParserCallBack<JSONObject, Player>) PlayerParser::playerToJsonObject;
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        };
    }

    public static ParserCallBack<?, TreeMap<Integer, VerifiedEntity>> allOutput(Object dataType){
        return switch (dataType) {
            case FileType.XML -> (ParserCallBack<org.w3c.dom.Document, TreeMap<Integer, VerifiedEntity>>) PlayerParser::playerToXmlElement;
            case FileType.TXT -> (ParserCallBack<ArrayList<String>, TreeMap<Integer, VerifiedEntity>>) PlayerParser::playerToArrayString;
            case FileType.DAT -> (ParserCallBack<ArrayList<Player>, TreeMap<Integer, VerifiedEntity>>) PlayerParser::playerToArrayEntity;
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        };
    }
    public static void parseResultSet(ResultSet resultSet, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
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

    public static void playerToUpdateStatement(PreparedStatement statement, DataOperation operation, Player player){
        try {
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

    public static void parseList(List<VerifiedEntity> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        for(VerifiedEntity verifiedEntity : list){
            parseVerifiedEntity(verifiedEntity, null, dataMap);
        }
    }


    public static void parseMongoDocument(Document document, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        dataMap.clear();
        Player player = new Player();
        player.setID(document.getInteger("id"));
        player.setName(document.getString("name"));
        player.setRegion(new Region(document.getString("region")));
        player.setServer(new Server(document.getString("server"), player.getRegion()));
        dataMap.put(player.getID(), player);
    }

    public static void playerToMongoDocument(Document document, DataOperation operation, Player player){
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

    public static void parseArrayListText(ArrayList<String> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        dataMap.clear();
        list.forEach(text -> {
            String[] player_txt = text.split(";");
            Player player = new Player();
            player.setID(Integer.parseInt(player_txt[0]));
            player.setRegion(new Region(player_txt[1]));
            player.setServer(new Server(player_txt[2], player.getRegion()));
            player.setName(player_txt[3]);
            dataMap.put(player.getID(),player);
        });
    }

    public static void playerToArrayString(ArrayList<String> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        for(VerifiedEntity entity : dataMap.values()){
            Player player = (Player) entity;
            list.add("%s;%s;%s;%s".formatted(player.getID(),player.getRegion(),player.getServer(),player.getName()));
        }
    }

    public static void parseVerifiedEntity(VerifiedEntity entity, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Player player = (Player) entity;
        dataMap.put(player.getID(),player);
    }

    public static void playerToArrayEntity(ArrayList<Player> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        for(VerifiedEntity entity : dataMap.values()){
            Player player = (Player) entity;
            list.add(player);
        }
    }

    public static void parseXmlElement(Element element, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
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

    public static void playerToXmlElement(org.w3c.dom.Document document, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Element rootElement = document.createElement("Player");
        document.appendChild(rootElement);
        for (VerifiedEntity entity : dataMap.values()) {
            Player player = (Player) entity;
            Element playerElement = document.createElement("player");
            playerElement.setAttribute("id", String.valueOf(player.getID()));
            xml_utils.createElementWithText(document, playerElement, "region", player.getRegion().toString());
            xml_utils.createElementWithText(document, playerElement, "server", player.getServer().toString());
            xml_utils.createElementWithText(document, playerElement, "name", player.getName());
            rootElement.appendChild(playerElement);
        }
    }

    public static void playerToBaseXQuery(String[] query, DataOperation operation, Player player){
        query[0] = switch (operation){
            case ADD -> "insert node <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player> into /Player"
                    .formatted(player.getID(), player.getRegion(), player.getServer(), player.getName());
            case MODIFY -> "replace node /Player/player[@id='%s'] with <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player>"
                    .formatted(player.getID(), player.getID(), player.getRegion(), player.getServer(), player.getName());
            case DELETE -> "delete node /Player/player[@id='%s']".formatted(player.getID());
            default -> throw new OperationException("Invalid DataOperation");
        };
    }

    public static void parseJsonObject(JSONObject jsonObject, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
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
    public static void playerToJsonObject(JSONObject jsonObject, DataOperation operation, Player player){
        jsonObject.put("id", player.getID());
        jsonObject.put("name", player.getName());
        jsonObject.put("region", player.getRegion().toString());
        jsonObject.put("server", player.getServer().toString());
        jsonObject.put("operation", operation.toString());
    }

}
