package data.http;

import Interface.PlayerCRUD;
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
import java.util.Map;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class PhpPlayerCRUD implements PlayerCRUD<PhpType> {
    private static final Logger logger = LoggerFactory.getLogger(PhpPlayerCRUD.class);
    ApiRequests api;
    private String url;
    private String readUrl;
    private String writeUrl;
    PhpType phpType;

    @Override
    public PlayerCRUD<PhpType> prepare(PhpType phpType) {
        this.phpType = phpType;
        if(phpType == PhpType.JSON){
            api = new ApiRequests();
            url = getProperty("phpURL");
            readUrl = getProperty("phpReadURL");
            writeUrl = getProperty("phpWriteURL");
            return this;
        }
        throw new HttpPhpException("Invalid php type");
    }

    @Override
    public void release() {
    }

    @Override
    public TreeMap<Integer, Player> read() {
        logger.info("Reading player data from {}", url + readUrl);
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
            logger.error("Failed to read data with cause: {}", e.getMessage());
            throw new HttpPhpException("Failed to read from target php server with cause: " + e.getMessage());
        }
        logger.info("Completed reading from PHP server");
        return playerMap;
    }

    //TODO: 更新逻辑有问题
    @Override
    @SuppressWarnings("unchecked")
    public void update(HashMap<Player, DataOperation> changed_player_map) {
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
                logger.info("Successfully updated data on PHP server");
            }
        } catch (IOException e) {
            logger.error("Failed to update data with cause: {}", e.getMessage());
            throw new HttpPhpException("Failed to update data on target php server with cause: " + e.getMessage());
        }
    }

    @Override
    public void export(TreeMap<Integer, Player> player_map) {
        TreeMap<Integer, Player> existed_player_map = read();
        HashMap<Player, DataOperation> export_player_map = new HashMap<>();
        for(Map.Entry<Integer, Player> idAndPlayer : existed_player_map.entrySet() ) {
            if(!player_map.containsKey(idAndPlayer.getKey())) {
                logger.info("Eliminating none existing player with id {}", idAndPlayer.getKey());
                export_player_map.put(idAndPlayer.getValue(), DataOperation.DELETE);
            }else{
                logger.info("Modifying existing player with id {}", idAndPlayer.getKey());
                export_player_map.put(player_map.get(idAndPlayer.getKey()), DataOperation.MODIFY);
            }
        }
        for(Map.Entry<Integer, Player> idAndPlayer : player_map.entrySet() ) {
            if(!existed_player_map.containsKey(idAndPlayer.getKey())) {
                logger.info("Adding new player with id {}", idAndPlayer.getKey());
                export_player_map.put(idAndPlayer.getValue(), DataOperation.ADD);
            }
        }
        update(export_player_map);
        logger.info("Completed exporting data to target php server");
    }
}
