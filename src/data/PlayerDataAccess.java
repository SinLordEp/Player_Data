package data;

import GUI.GeneralText;
import data.database.PlayerDBA;
import Interface.GeneralDataAccess;
import data.database.SqlDialect;
import exceptions.*;
import model.DatabaseInfo;
import model.Player;
import data.file.PlayerFileReader;
import data.file.PlayerFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class PlayerDataAccess extends GeneralDataAccess {
    private TreeMap<Integer, Player> player_map = new TreeMap<>();
    private final HashMap<Player, DataOperation> changed_player_map = new HashMap<>();
    private HashMap<String, String[]> region_server_map;
    private final PlayerDBA playerDBA;
    private final PlayerFileReader fileReader;
    private final PlayerFileWriter fileWriter;
    private static Logger logger = LoggerFactory.getLogger(PlayerDataAccess.class);

    public PlayerDataAccess() {
        fileReader = new PlayerFileReader();
        fileWriter = new PlayerFileWriter();
        playerDBA = new PlayerDBA();
    }

    public void initializeRegionServer(){
        region_server_map = PlayerFileReader.read_region_server();
    }

    @Override
    @SuppressWarnings("unchecked")
    public DatabaseInfo getDefaultDatabaseInfo(SqlDialect dialect) throws ConfigErrorException {
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setDialect(dialect);
        HashMap<String, Object> default_info = null;
        URL resource = getClass().getResource(getProperty("defaultPlayerSQL"));
        if (resource != null) {
            try(InputStream inputStream = resource.openStream()){
                Yaml yaml = new Yaml();
                default_info = yaml.load(inputStream);
            }catch (IOException e){
                throw new ConfigErrorException("Error loading default database info");
            }
        }
        if(default_info == null){
            throw new ConfigErrorException("Default database info is null");
        }
        switch (dialect) {
            case MYSQL:
                HashMap<String,Object> mysql_info = (HashMap<String, Object>) default_info.get("MYSQL");
                databaseInfo.setUrl((String) mysql_info.get("text_url"));
                databaseInfo.setPort((String) mysql_info.get("text_port"));
                databaseInfo.setDatabase((String) mysql_info.get("text_database"));
                databaseInfo.setUser((String) mysql_info.get("text_user"));
                databaseInfo.setPassword((String) mysql_info.get("text_pwd"));
                break;
            case SQLITE:
                HashMap<String,Object> sqlite_info = (HashMap<String, Object>) default_info.get("SQLITE");
                databaseInfo.setUrl((String) sqlite_info.get("text_url"));
                break;
        }
        return databaseInfo;
    }

    public boolean connectDB(DatabaseInfo databaseInfo) {
        return playerDBA.connect(databaseInfo);
    }

    public void disconnectDB(){
        playerDBA.disconnect(dataSource);
    }

    @SuppressWarnings("unchecked")
    public void read() {
        try {
            player_map = switch (dataSource){
                case NONE -> null;
                case FILE -> (TreeMap<Integer, Player>) fileReader.read(fileType, file_path);
                case DATABASE, HIBERNATE -> playerDBA.read(dataSource);
            };
            if(player_map != null && !player_map.isEmpty()){
                isDataValid();
            }
        } catch (Exception e) {
            player_map = new TreeMap<>();
            dataSource = DataSource.NONE;
            throw new OperationException("Failed to read data. Cause: " + e.getMessage());
        }
    }

    public void save(){
        try{
            switch (dataSource){
                case FILE:
                    fileWriter.write(file_path, player_map);
                    break;
                case DATABASE, HIBERNATE:
                    playerDBA.connect(databaseInfo);
                    playerDBA.update(dataSource, changed_player_map);
                    break;
            }
        } catch (Exception e) {
            throw new OperationException("Failed to save data. Cause: " + e.getMessage());
        }
    }

    public void add(Player player) {
        switch(dataSource){
            //case FILE ->
            case DATABASE, HIBERNATE -> changed_player_map.put(player, DataOperation.ADD);
        }
        player_map.put(player.getID(), player);
    }

    public void modify(Player player) {
        switch(dataSource){
            //case FILE ->
            case DATABASE, HIBERNATE  -> changed_player_map.put(player, DataOperation.MODIFY);
        }
        player_map.put(player.getID(), player);
    }

    public void delete(int selected_player_id) {
        switch(dataSource){
            //case FILE ->
            case DATABASE, HIBERNATE -> changed_player_map.put(player_map.get(selected_player_id), DataOperation.DELETE);
        }
        player_map.remove(selected_player_id);
    }

    public void export() {
        String target_extension = getExtension(fileType);
        String target_path = getPath();
        String target_name = GeneralText.getDialog().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        fileWriter.write(target_path, player_map);
    }

    public void exportDB(DataSource dataSource) {
        playerDBA.export(dataSource, player_map);
    }

    public boolean isEmpty(){
        return player_map.isEmpty();
    }

    public TreeMap<Integer, Player> getPlayerMap() {
        return player_map;
    }

    public void isPlayerInvalid(Player player){
        if(region_server_map == null){
            throw new DataCorruptedException("region_server_map is null");
        }
        if(!region_server_map.containsKey(player.getRegion())){
            throw new DataCorruptedException("Player's region is not found");
        }
        boolean server_valid = false;
        for(String server : region_server_map.get(player.getRegion())){
            if (server.equals(player.getServer())) {
                server_valid = true;
                break;
            }
        }
        if(!server_valid){
            throw new DataCorruptedException("Player's server is not found");
        }
        if(player.getID() <= 0){
            throw new DataCorruptedException("Player's ID is invalid");
        }
        if(player.getName().isBlank()){
            throw new DataCorruptedException("Player's name is invalid");
        }
    }

    public void isDataValid(){
        for(Player player : player_map.values()){
            isPlayerInvalid(player);
        }
    }

    public HashMap<String, String[]> getRegion_server_map() {
        return region_server_map;
    }

    public Player getPlayer(int id){
        Player player = player_map.get(id);
        Player playerCopy = new Player();
        playerCopy.setID(id);
        playerCopy.setName(player.getName());
        playerCopy.setRegion(player.getRegion());
        playerCopy.setServer(player.getServer());
        return playerCopy;
    }

    public void clearData(){
        player_map.clear();
    }
}
