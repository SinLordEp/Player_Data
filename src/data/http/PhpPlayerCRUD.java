package data.http;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
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
import java.util.Map;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class PhpPlayerCRUD implements PlayerCRUD<PhpType> {
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
    public PlayerCRUD<PhpType> read(ParserCallBack<PhpType> data) {
        try {
            String rawJson = api.getRequest(url + readUrl);
            JSONObject parsedJson = (JSONObject) JSONValue.parse(rawJson);
            if(parsedJson == null) {
                throw new DataCorruptedException("Data is null");
            }
            if("error".equals(parsedJson.get("status").toString())) {
                throw new HttpPhpException(parsedJson.get("message").toString());
            }
            JSONArray playersArray = (JSONArray) parsedJson.get("players");
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
                player_map.put(player.getID(), player);
            }
        } catch (IOException e) {
            throw new HttpPhpException(e.getMessage());
        }
        return this;
    }

    //TODO: 更新逻辑有问题
    @Override
    @SuppressWarnings("unchecked")
    public PlayerCRUD<PhpType> update(HashMap<Player, DataOperation> changed_player_map) {
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
        } catch (IOException e) {
            throw new HttpPhpException(e.getMessage());
        }
        return this;
    }

    @Override
    public PlayerCRUD<PhpType> export(ParserCallBack<R> parser, TreeMap<Integer, VerifiedEntity> dataMap) {
        TreeMap<Integer, Player> existed_player_map = new TreeMap<>();
        read(existed_player_map);
        HashMap<Player, DataOperation> export_player_map = new HashMap<>();
        for(Map.Entry<Integer, Player> idAndPlayer : existed_player_map.entrySet() ) {
            if(!dataMap.containsKey(idAndPlayer.getKey())) {
                export_player_map.put(idAndPlayer.getValue(), DataOperation.DELETE);
            }else{
                export_player_map.put(dataMap.get(idAndPlayer.getKey()), DataOperation.MODIFY);
            }
        }
        for(Map.Entry<Integer, Player> idAndPlayer : dataMap.entrySet() ) {
            if(!existed_player_map.containsKey(idAndPlayer.getKey())) {
                export_player_map.put(idAndPlayer.getValue(), DataOperation.ADD);
            }
        }
        update(export_player_map);
        return this;
    }
}
