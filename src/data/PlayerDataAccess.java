package data;

import data.DB.PlayerDBA;
import GUI.GeneralMenu;
import model.Player;
import data.file.PlayerFileReader;
import data.file.PlayerFileWriter;

import javax.swing.*;
import java.util.HashMap;
import java.util.TreeMap;

public class PlayerDataAccess extends GeneralDataAccess {
    private TreeMap<Integer, Player> player_map = new TreeMap<>();
    private final HashMap<String, String[]> region_server_map;
    private final String[] region_list;
    private final PlayerDBA DBAccess;
    private final PlayerFileReader fileReader;
    private final PlayerFileWriter fileWriter;

    public PlayerDataAccess() throws Exception {
        fileReader = new PlayerFileReader();
        fileWriter = new PlayerFileWriter();
        DBAccess = new PlayerDBA();
        region_server_map = PlayerFileReader.read_region_server();
        region_list = region_server_map.keySet().toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    public void read() throws Exception {
        if(isDBOnly()){
            player_map = DBAccess.read();
        }else{
            player_map = (TreeMap<Integer, Player>) fileReader.read(file_path);
        }
        setData_changed(false);
    }

    public void write() throws Exception {
        if(!DBOnly){
            fileWriter.write(file_path, player_map);
        }
    }

    public void refresh() throws Exception {
        if(isData_changed()) {
            read();
            if(!isPlayerMap_Valid()){
                throw new Exception("Player data corrupted");
            }
        }
    }

    public void export() throws Exception {
        if(fileWriter != null){
            String target_extension = choose_extension();
            String target_path = get_path("path");
            String target_name = GeneralMenu.universalInput("Input new data.file name");
            target_path += "/" + target_name + target_extension;
            fileWriter.write(target_path, player_map);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public void export_DB() throws Exception {
        if(!DBAccess.connected()){
            GeneralMenu.message_popup("Database is not connected");
            return;
        }
        DBAccess.wipe();
        for(Player player : player_map.values()){
            DBAccess.add(player);
        }
        GeneralMenu.message_popup("Players from map are added to data.DB");
    }

    public String delete(int selected_player_id) throws Exception {
        player_map.remove(selected_player_id);
        if(DBAccess.connected()){
            DBAccess.delete(selected_player_id);
        }
        setData_changed(true);
        return "Player with ID " + selected_player_id + " is deleted";
    }

    public String add(Player player) throws Exception {
        if(isPlayer_Invalid(player)) {
            throw new Exception("Player data is invalid");
        }
        player_map.put(player.getID(), player);
        if(DBAccess.connected()){
            DBAccess.add(player);
        }
        setData_changed(true);
        write();
        return "Player with ID " + player.getID() + " is added";
    }

    public String update(Player player) throws Exception {
        if(isPlayer_Invalid(player)) {
            throw new Exception("Player data is invalid");
        }
        player_map.put(player.getID(), player);
        if(DBAccess.connected()){
            DBAccess.modify(player);
        }
        write();
        return  "Player with ID " + player.getID() + " is modified";
    }

    public String dataSource() {
        String data_source = "Data Source: ";
        if(DBOnly){
            data_source += "DataBase";
        }else{
            data_source += file_path.substring(file_path.lastIndexOf(".")) + " File";
        }
        return data_source;
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
            JOptionPane.showMessageDialog(null, "Region server is null!");
            return true;
        }
        if(!region_server_map.containsKey(player.getRegion())){
            JOptionPane.showMessageDialog(null, "Player Region doesn't exist!");
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
            GeneralMenu.message_popup("Player Server doesn't exist!");
            return true;
        }

        if(player.getID() <= 0){
            GeneralMenu.message_popup("Player ID is ilegal");
            return true;
        }

        if(player_map == null) return false;

        if(player.getName().isBlank()){
            GeneralMenu.message_popup("Player Name is blank");
            return true;
        }
        return false;
    }

    public boolean isPlayerMap_Valid(){
        if(player_map == null){
            GeneralMenu.message_popup("No player data registered");
            return true;
        }
        for(Player player : player_map.values()){
            if(isPlayer_Invalid(player)){
                return false;
            }
        }
        return true;
    }

    public PlayerDBA getDBAccess() {
        return DBAccess;
    }
}
