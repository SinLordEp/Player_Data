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
    private String url, readUrl, writeUrl;

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

    public void update(HashMap<Player, DataOperation> changed_player_map){

    }
}
