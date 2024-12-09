package data;

import GUI.GeneralText;
import data.database.PlayerDBA;
import data.database.SqlDialect;
import data.http.PlayerPhp;
import exceptions.*;
import model.DatabaseInfo;
import model.Player;
import data.file.PlayerFileReader;
import data.file.PlayerFileWriter;
import model.Region;
import model.Server;
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
    private HashMap<Region, Server[]> region_server_map;
    private final PlayerDBA playerDBA;
    private final PlayerPhp playerPhp;
    private final PlayerFileReader fileReader;
    private final PlayerFileWriter fileWriter;
    private static final Logger logger = LoggerFactory.getLogger(PlayerDataAccess.class);

    public PlayerDataAccess() {
        logger.info("PlayerDataAccess: Instantiated");
        fileReader = new PlayerFileReader();
        fileWriter = new PlayerFileWriter();
        playerDBA = new PlayerDBA();
        playerPhp = new PlayerPhp();
    }

    public void initializeRegionServer(){
        logger.info("Initializing region server: Reading config file...");
        try {
            DatabaseInfo info = getDefaultDatabaseInfo(SqlDialect.SQLITE);
            info.setDataSource(DataSource.HIBERNATE);
            playerDBA.connect(info);
        } catch (ConfigErrorException e) {
            throw new RuntimeException(e);
        }
        region_server_map = playerDBA.readRegionServer();
        logger.info("Initializing region server: Region server map updated!");
    }

    @Override
    @SuppressWarnings("unchecked")
    public DatabaseInfo getDefaultDatabaseInfo(SqlDialect dialect) throws ConfigErrorException {
        logger.info("Get default database info: Reading default database info for dialect: {}", dialect);
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
        logger.info("Get default database info: Finished reading database info!");
        return databaseInfo;
    }

    public boolean connectDB(DatabaseInfo databaseInfo) {
        logger.info("Connect DB: Calling DBA to connect...");
        return playerDBA.connect(databaseInfo);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void read() {
        logger.info("Read: Reading data from {}", dataSource);
        try {
            switch (dataSource){
                case NONE :
                    logger.info("Read: Data source is {}, resetting player map...", dataSource);
                    player_map = new TreeMap<>();
                    break;
                case FILE:
                    logger.info("Read: Data source is {}, calling file reader...", dataSource);
                    player_map = (TreeMap<Integer, Player>) fileReader.read(fileType, file_path);
                    break;
                case DATABASE, HIBERNATE :
                    logger.info("Read: Data source is {}, calling DBA...", dataSource);
                    player_map = playerDBA.read(dataSource);
                    break;
                case PHP:
                    logger.info("Read: Data source is {}, calling PHP...", dataSource);
                    player_map = playerPhp.read(dataType);
                    break;
            }
            if(player_map != null && !player_map.isEmpty()){
                isDataValid();
            }
        } catch (Exception e) {
            player_map = new TreeMap<>();
            dataSource = DataSource.NONE;
            throw new OperationException("Read: Failed to read data with cause: " + e.getMessage());
        }
        logger.info("Read: Finished reading data!");
    }

    @Override
    public void save(){
        logger.info("Save: Saving data to {}", dataSource);
        if(changed_player_map.isEmpty() && dataSource != DataSource.FILE){
            logger.info("Save: No data needed to save!");
            return;
        }
        try{
            switch (dataSource){
                case FILE:
                    fileWriter.write(file_path, player_map);
                    break;
                case DATABASE, HIBERNATE:
                    playerDBA.connect(databaseInfo);
                    playerDBA.update(dataSource, changed_player_map);
                    playerDBA.disconnect(dataSource);
                    changed_player_map.clear();
                    break;
                case PHP:
                    playerPhp.update(changed_player_map);
                    changed_player_map.clear();
                    break;
            }
        } catch (Exception e) {
            throw new OperationException("Failed to save data with cause: " + e.getMessage());
        }
        logger.info("Save: Finished saving data!");
    }

    public void add(Player player) {
        logger.info("Add: Adding player with ID: {}", player.getID());
        switch(dataSource){
            //case FILE ->
            case DATABASE, HIBERNATE:
                logger.info("Add: Adding player to changed player map");
                changed_player_map.put(player, DataOperation.ADD);
                break;
        }
        logger.info("Add: Adding player to current player map");
        player_map.put(player.getID(), player);
        logger.info("Add: Finished adding player!");
    }

    public void modify(Player player) {
        logger.info("Modify: Modifying player with ID: {}", player.getID());
        switch(dataSource){
            //case FILE ->
            case DATABASE, HIBERNATE, PHP :
                logger.info("Modify: Adding modified player with ID: {} to changed player map", player.getID());
                changed_player_map.put(player, DataOperation.MODIFY);
        }
        logger.info("Modify: Adding modified player with ID: {} to current player map", player.getID());
        player_map.put(player.getID(), player);
        logger.info("Modify: Finished modifying player!");
    }

    public void delete(int selected_player_id) {
        logger.info("Delete: Deleting player with ID: {}", selected_player_id);
        switch(dataSource){
            //case FILE ->
            case DATABASE, HIBERNATE :
                logger.info("Delete: Adding deleted player with ID: {} to changed player map", selected_player_id);
                changed_player_map.put(player_map.get(selected_player_id), DataOperation.DELETE);
        }
        logger.info("Delete: Deleting player with ID: {} from current player map", selected_player_id);
        player_map.remove(selected_player_id);
        logger.info("Delete: Finished deleting player!");
    }

    public void exportFile() {
        logger.info("Export file: Building extension...");
        String target_extension = getExtension(fileType);
        String target_path = getPath();
        String target_name = GeneralText.getDialog().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        logger.info("Export file: Target path is set to {}", target_path);
        fileWriter.write(target_path, player_map);
        logger.info("Export file: Finished exporting file!");
    }

    public void exportDB(DataSource dataSource) {
        logger.info("Export DB: Calling DBA...");
        playerDBA.export(dataSource, player_map);
    }

    public boolean isEmpty(){
        return player_map.isEmpty();
    }

    public TreeMap<Integer, Player> getPlayerMap() {
        return player_map;
    }

    private void isPlayerInvalid(Player player){
        if(region_server_map.isEmpty()){
            throw new DataCorruptedException("region_server_map is null");
        }
        if(!region_server_map.containsKey(player.getRegion())){
            throw new DataCorruptedException("Player's region is not found");
        }
        boolean server_valid = false;
        for(Server server : region_server_map.get(player.getRegion())){
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

    private void isDataValid(){
        logger.info("isDataValid: Validating imported data...");
        for(Player player : player_map.values()){
            isPlayerInvalid(player);
        }
        logger.info("isDataValid: Data is valid");
    }

    public HashMap<Region, Server[]> getRegion_server_map() {
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
        logger.info("Clearing current data...");
        player_map.clear();
        logger.info("Player data is cleared successfully");
    }
}
