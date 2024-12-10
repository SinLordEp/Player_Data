package data.http;

import Interface.GeneralPhp;
import data.DataOperation;
import exceptions.DataCorruptedException;
import exceptions.HttpPhpException;
import model.Player;
import model.Region;
import model.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class PlayerPhp implements GeneralPhp<SortedMap<Integer,Player>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayerPhp.class);
    ApiRequests api;
    private final String url;
    private final String readUrl;
    private final String writeUrl;

    public PlayerPhp() {
        logger.info("PlayerPhp: Instantiated");
        api = new ApiRequests();
        url = "http://localhost/sin/player_data/";
        readUrl = "read_player.php";
        writeUrl = "write_player.php";
    }

    @Override
    public TreeMap<Integer, Player> read(DataType dataType) {
        logger.info("Read: Reading player data in form of {}", dataType);
        return switch (dataType){
            case NONE -> new TreeMap<>();
            case JSON -> read_json();
        };
    }

    private TreeMap<Integer, Player> read_json() {
        logger.info("Read JSON: Reading player data from {}", url + readUrl);
        TreeMap<Integer, Player> playerMap = new TreeMap<>();
        try {
            String rawJson = api.getRequest(url + readUrl);
            JSONObject parsedJson = (JSONObject) JSONValue.parse(rawJson);
            if(parsedJson == null) {
                throw new DataCorruptedException("Failed to parse data from target php with cause: Data is null");
            }
            if("error".equals(parsedJson.get("status").toString())) {
                throw new HttpPhpException(parsedJson.get("message").toString());
            }
            JSONArray playersArray = (JSONArray) parsedJson.get("players");
            if(playersArray.isEmpty()) {
                logger.error("Read JSON: No player datas found");
                throw new DataCorruptedException("Failed to parse data from target php with cause: No players found");
            }
            for (Object object : playersArray) {
                Player player = new Player();
                JSONObject playerObject = (JSONObject) object;
                player.setID(Integer.parseInt(playerObject.get("id").toString()));
                player.setName(playerObject.get("name").toString());
                player.setRegion(new Region(playerObject.get("region").toString()));
                player.setServer(new Server(playerObject.get("server").toString(), player.getRegion()));
                playerMap.put(player.getID(), player);
            }
        } catch (IOException e) {
            logger.error("Read JSON: Failed to read data with cause: {}", e.getMessage());
            throw new HttpPhpException("Failed to read from target php server with cause: " + e.getMessage());
        }
        logger.info("Read JSON: Finished reading from PHP server");
        return playerMap;
    }

    @Override
    public void export(DataType dataType, SortedMap<Integer, Player> player_map) {
        logger.info("Export: Exporting player data in form of {}", dataType);
        TreeMap<Integer, Player> existed_player_map = read(DataType.JSON);
        HashMap<Player, DataOperation> export_player_map = new HashMap<>();
        for(Player player : existed_player_map.values()) {
            if(!player_map.containsKey(player.getID())) {
                logger.info("Export: Eliminated none existing player with id {}", player.getID());
                export_player_map.put(player, DataOperation.DELETE);
            }else{
                logger.info("Export: Modifying existing player with id {}", player.getID());
                export_player_map.put(player_map.get(player.getID()), DataOperation.MODIFY);
            }
        }
        for(Player player : player_map.values()) {
            if(!existed_player_map.containsKey(player.getID())) {
                logger.info("Export: Adding new player with id {}", player.getID());
                export_player_map.put(player, DataOperation.ADD);
            }
        }
        logger.info("Export: Calling update to apply changes...");
        update(export_player_map);
        logger.info("Export: Finished exporting data to target php server");
    }

    @SuppressWarnings("unchecked")
    public void update(HashMap<Player, DataOperation> changed_player_map){
        logger.info("Update: Updating PHP server data with changed player map...");
        JSONArray playerArray = new JSONArray();
        for(Player player : changed_player_map.keySet()) {
            JSONObject playerObject = new JSONObject();
            playerObject.put("id", player.getID());
            playerObject.put("name", player.getName());
            playerObject.put("region", player.getRegion().toString());
            playerObject.put("server", player.getServer().toString());
            playerObject.put("operation", changed_player_map.get(player).toString());
            playerArray.add(playerObject);
        }
        try {
            String postRequest = api.postRequest(url + writeUrl, playerArray.toJSONString());
            JSONObject response = (JSONObject) JSONValue.parse(postRequest);
            if(response == null) {
                throw new HttpPhpException("Json was sent to server but did not receive a correct response");
            }
            if("error".equals(response.get("status").toString())) {
                throw new HttpPhpException(response.get("message").toString());
            }
            if("success".equals(response.get("status").toString())) {
                logger.info("Update: Successfully updated data on PHP server");
            }
        } catch (IOException e) {
            logger.error("Update: Failed to update data with cause: {}", e.getMessage());
            throw new HttpPhpException("Failed to update data on target php server with cause: " + e.getMessage());
        }
        logger.info("Update: Finished updating data on PHP server");
    }
}
