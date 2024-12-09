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

import java.io.IOException;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class PlayerPhp implements GeneralPhp<SortedMap<?,?>> {
    ApiRequests api;
    private final String url;
    private final String readUrl;
    private final String writeUrl;

    public PlayerPhp() {
        api = new ApiRequests();
        url = "http://localhost/sin/player_data/";
        readUrl = "read_player.php";
        writeUrl = "write_player.php";
    }

    @Override
    public TreeMap<Integer, Player> read(DataType dataType) {
        return switch (dataType){
            case NONE -> new TreeMap<>();
            case JSON -> read_json();
        };
    }

    private TreeMap<Integer, Player> read_json() {
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
            throw new HttpPhpException("Failed to read from target php server with cause: " + e.getMessage());
        }
        return playerMap;
    }

    @Override
    public void write(SortedMap<?, ?> data) {

    }

    @SuppressWarnings("unchecked")
    public void update(HashMap<Player, DataOperation> changed_player_map){
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
            System.out.println(response.toJSONString());
        } catch (IOException e) {
            throw new HttpPhpException("Failed to write to target php server with cause: " + e.getMessage());
        }
    }
}
