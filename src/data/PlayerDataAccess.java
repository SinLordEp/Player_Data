package data;

import GUI.GeneralDialog;
import GUI.Player.PlayerDialog;
import data.database.PlayerDBA;
import Interface.GeneralDataAccess;
import data.database.SqlDialect;
import exceptions.*;
import model.Player;
import data.file.PlayerFileReader;
import data.file.PlayerFileWriter;
import org.yaml.snakeyaml.Yaml;

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
    private String[] region_list;
    private final PlayerDBA playerDBA;
    private final PlayerFileReader fileReader;
    private final PlayerFileWriter fileWriter;

    public PlayerDataAccess() {
        fileReader = new PlayerFileReader();
        fileWriter = new PlayerFileWriter();
        playerDBA = new PlayerDBA();
    }

    public void initializeRegionServer(){
        region_server_map = PlayerFileReader.read_region_server();
        region_list = region_server_map.keySet().toArray(new String[0]);
    }
    @Override
    @SuppressWarnings("unchecked")
    public HashMap<String, String> getDefaultDatabaseInfo(SqlDialect dialect) throws ConfigErrorException {
        HashMap<String, String> login_info = new HashMap<>();
        HashMap<String,Object> default_info = null;
        URL resource = getClass().getResource(getProperty("defaultPlayerSQL"));
        if (resource != null) {
            try(InputStream inputStream = resource.openStream()){
                Yaml yaml = new Yaml();
                default_info = yaml.load(inputStream);
            }catch (Exception e){
                throw new ConfigErrorException("Error loading default database info");
            }
        }
        if(default_info == null){
            throw new ConfigErrorException("Default database info is null");
        }
        switch (dialect) {
            case MYSQL:
                HashMap<String,Object> mysql_info = (HashMap<String, Object>) default_info.get("MYSQL");
                login_info.put("text_url", (String) mysql_info.get("text_url"));
                login_info.put("text_port",(String) mysql_info.get("text_port"));
                login_info.put("text_database",(String) mysql_info.get("text_database"));
                login_info.put("text_user",(String) mysql_info.get("text_user"));
                login_info.put("text_pwd",(String) mysql_info.get("text_pwd"));
                break;
            case SQLITE:
                HashMap<String,Object> sqlite_info = (HashMap<String, Object>) default_info.get("SQLITE");
                login_info.put("text_url",(String) sqlite_info.get("text_url"));
                break;
        }
        return login_info;
    }

    public boolean connectDB() {
        return playerDBA.connect(dataSource);
    }

    public void setLogin_info(HashMap<String, String> login_info) {
        playerDBA.setLogin_info(login_info);
    }

    public void setSQLDialect(SqlDialect dialect){
        playerDBA.setDialect(dialect);
    }

    public SqlDialect getSQLDialect(){
        return playerDBA.getDialect();
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
            if(player_map != null && !isDataValid()){
                throw new DataCorruptedException("Data is corrupted");
            }
        } catch (Exception e) {
            GeneralDialog.getDialog().message("Failed to read data\n" + e.getMessage());
            player_map = new TreeMap<>();
            dataSource = DataSource.NONE;
        }
    }

    public void save(){
        try{
            switch (dataSource){
                case FILE -> fileWriter.write(file_path, player_map);
                case DATABASE, HIBERNATE -> playerDBA.update(dataSource, changed_player_map);
            }
        } catch (Exception e) {
            GeneralDialog.getDialog().message("Failed to save data\n" + e.getMessage());
        }
        GeneralDialog.getDialog().message(GeneralDialog.getDialog().getPopup("data_saved") + dataSource);
    }

    public void add() {
        Player player = new Player();
        player.setRegion((String) PlayerDialog
                .getDialog()
                .selectionDialog("region_menu", region_list));
        player.setServer((String) PlayerDialog
                .getDialog()
                .selectionDialog("server_menu", region_server_map.get(player.getRegion())));
        player.setID(createID());
        player.setName(PlayerDialog
                .getDialog()
                .input("player_name"));
        switch(dataSource){
            //case FILE ->
            case DATABASE, HIBERNATE -> changed_player_map.put(player, DataOperation.ADD);
        }
        player_map.put(player.getID(), player);
        PlayerDialog.getDialog().popup( "added_player");
    }

    private int createID() {
        while (true) {
            try {
                int id = Integer.parseInt(PlayerDialog.getDialog().input("id"));
                if (player_map.containsKey(id)) {
                    PlayerDialog.getDialog().popup("duplicate_player");
                } else {
                    return id;
                }
            } catch (NumberFormatException e) {
                PlayerDialog.getDialog().popup("number_format_invalid");
            }
        }
    }

    public void modify(int selected_player_id) {
        Player player = player_map.get(selected_player_id);
        switch((int)PlayerDialog.getDialog().selectionDialog("modify_player")){
            // After changing region the server has to be changed too.
            case 0: player.setRegion((String) PlayerDialog.getDialog()
                    .selectionDialog("region_menu", region_list));
            case 1: player.setServer((String) PlayerDialog.getDialog()
                    .selectionDialog("server_menu", region_server_map.get(player.getRegion())));
                break;
            case 2: player.setName(PlayerDialog
                    .getDialog()
                    .input("player_name"));
                break;
            case 3:
                player.setRegion((String) PlayerDialog.getDialog()
                        .selectionDialog("region_menu", region_list));
                player.setServer((String) PlayerDialog.getDialog()
                        .selectionDialog("server_menu",region_server_map.get(player.getRegion())));
                player.setName(PlayerDialog
                        .getDialog()
                        .input("player_name"));
                break;
        }
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
        PlayerDialog.getDialog().popup( "deleted_player");
    }

    public void export() {
        String target_extension = getExtension(fileType);
        String target_path = getPath();
        String target_name = GeneralDialog.getDialog().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        fileWriter.write(target_path, player_map);
        PlayerDialog.getDialog().popup("exported_file");
    }

    public void exportDB(DataSource dataSource, SqlDialect dialect, HashMap<String,String> login_info) {
        PlayerDBA export_playerDBA = new PlayerDBA();
        export_playerDBA.setDialect(dialect);
        export_playerDBA.setLogin_info(login_info);
        if(!export_playerDBA.connect(dataSource)){
            PlayerDialog.getDialog().popup("db_not_connected");
            return;
        }
        export_playerDBA.export(dataSource, player_map);
    }

    public boolean isEmpty(){
        return player_map == null;
    }

    public TreeMap<Integer, Player> getPlayerMap() {
        return player_map;
    }

    public boolean isPlayerInvalid(Player player){
        if(region_server_map == null){
            PlayerDialog.getDialog().popup("region_server_null");
            return true;
        }
        if(!region_server_map.containsKey(player.getRegion())){
            PlayerDialog.getDialog().popup("region_invalid");
            return true;
        }
        boolean server_valid = false;
        for(String server : region_server_map.get(player.getRegion())){
            if (server.equals(player.getServer())) {
                server_valid = true;
                break;
            }
        }
        if(!server_valid){
            PlayerDialog.getDialog().popup("server_invalid");
            return true;
        }

        if(player.getID() <= 0){
            PlayerDialog.getDialog().popup("id_invalid");
            return true;
        }

        if(player_map == null) {
            return false;
        }

        if(player.getName().isBlank()){
            PlayerDialog.getDialog().popup("name_invalid");
            return true;
        }
        return false;
    }

    public boolean isDataValid(){
        if(player_map == null){
            PlayerDialog.getDialog().popup("player_map_null");
            return true;
        }
        for(Player player : player_map.values()){
            if(isPlayerInvalid(player)){
                return false;
            }
        }
        return true;
    }

}
