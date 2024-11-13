package data;

import GUI.Player.PlayerDialog;
import data.database.PlayerDBA;
import model.Player;
import data.file.PlayerFileReader;
import data.file.PlayerFileWriter;

import java.util.HashMap;
import java.util.TreeMap;

public class PlayerDataAccess extends GeneralDataAccess {
    private TreeMap<Integer, Player> player_map = new TreeMap<>();
    private final HashMap<String, String[]> region_server_map;
    private final String[] region_list;
    private final PlayerDBA playerDBA;
    private final PlayerFileReader fileReader;
    private final PlayerFileWriter fileWriter;

    public PlayerDataAccess() throws Exception {
        fileReader = new PlayerFileReader();
        fileWriter = new PlayerFileWriter();
        playerDBA = new PlayerDBA();
        region_server_map = PlayerFileReader.read_region_server();
        region_list = region_server_map.keySet().toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    public void read() throws Exception {
        if(!isData_changed()){
            return;
        }
        if(DB_source()){
            player_map = playerDBA.read();
        }else{
            player_map = (TreeMap<Integer, Player>) fileReader.read(file_path);
        }
        setData_changed(false);
    }

    public void write() throws Exception {
        if(!DB_source){
            fileWriter.write(file_path, player_map);
        }
    }

    public void refresh() throws Exception {
        read();
        if(!isPlayerMap_Valid()){
            throw new Exception("Player data corrupted");
        }
    }

    public void export() throws Exception {
        if(fileWriter != null){
            String target_extension = choose_extension();
            String target_path = get_path("path");
            String target_name = PlayerDialog.get().input("new_file_name");
            target_path += "/" + target_name + target_extension;
            fileWriter.write(target_path, player_map);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public void export_DB(){
        if(!playerDBA.connected()){
            PlayerDialog.get().popup("db_not_connected");
            return;
        }
        playerDBA.update("import", player_map);
        PlayerDialog.get().popup("exported_db");
    }

    public void delete(int selected_player_id) {
        if(playerDBA.connected()){
            playerDBA.update("remove",player_map.get(selected_player_id));
        }
        player_map.remove(selected_player_id);
        setData_changed(true);
        PlayerDialog.get().popup( "deleted_player");
    }

    public void add(Player player) throws Exception {
        if(isPlayer_Invalid(player)) {
            throw new Exception("Player data is invalid");
        }
        player_map.put(player.getID(), player);
        if(playerDBA.connected()){
            playerDBA.update("add",player);
        }
        setData_changed(true);
        write();
        PlayerDialog.get().popup( "added_player");
    }

    public void update(Player player) throws Exception {
        if(isPlayer_Invalid(player)) {
            throw new Exception("Player data is invalid");
        }
        player_map.put(player.getID(), player);
        if(playerDBA.connected()){
            playerDBA.update("update",player);
        }
        write();
        PlayerDialog.get().popup( "modified_player");
    }

    public boolean isEmpty(){
        return player_map == null;
    }

    public TreeMap<Integer, Player> getPlayer_map() {
        return player_map;
    }

    public String[] getRegion_list() {
        return region_list;
    }

    public String[] getServer_list(String region){
        return region_server_map.get(region);
    }

    public boolean containsKey(int ID){
        if(player_map == null){
            return false;
        }else{
            return player_map.containsKey(ID);
        }
    }

    public boolean isPlayer_Invalid(Player player){
        if(region_server_map == null){
            PlayerDialog.get().popup("region_server_null");
            return true;
        }
        if(!region_server_map.containsKey(player.getRegion())){
            PlayerDialog.get().popup("region_invalid");
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
            PlayerDialog.get().popup("server_invalid");
            return true;
        }

        if(player.getID() <= 0){
            PlayerDialog.get().popup("id_invalid");
            return true;
        }

        if(player_map == null) return false;
        if(player.getName().isBlank()){
            PlayerDialog.get().popup("name_invalid");
            return true;
        }
        return false;
    }

    public boolean isPlayerMap_Valid(){
        if(player_map == null){
            PlayerDialog.get().popup("player_map_null");
            return true;
        }
        for(Player player : player_map.values()){
            if(isPlayer_Invalid(player)){
                return false;
            }
        }
        return true;
    }

    public void configure_db(String URL, String port, String database, String user, String password) {
        playerDBA.setURL(URL + ":" + port + "/" + database);
        playerDBA.setUser(user);
        playerDBA.setPassword(password);
    }

    public void configure_db(String URL) {
        playerDBA.setURL(URL);
    }

    public boolean connect_db() {
        if(playerDBA.connect()){
            file_path = null;
            return true;
        }else return false;
    }

    public boolean disconnect_db(){
        if(DB_source){
            player_map = null;
        }
        DB_source = false;
        return true;
    }

}
