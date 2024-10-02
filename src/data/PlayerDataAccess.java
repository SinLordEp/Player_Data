package data;

import DB.PlayerDBA;
import GUI.GeneralMenu;
import model.Player;
import file.PlayerFileReader;
import file.PlayerFileWriter;

import javax.swing.*;
import java.util.HashMap;
import java.util.TreeMap;

public class PlayerDataAccess extends GeneralDataAccess {
    private TreeMap<Integer, Player> player_map = null;
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
        if(isDB()){
            player_map = DBAccess.read();
        }else{
            player_map = (TreeMap<Integer, Player>) fileReader.read(file_path);
        }
        setData_changed(false);
    }

    public void write() throws Exception {
        if(fileWriter != null && !isDB){
            fileWriter.write(file_path, player_map);
        }else {
            throw new IllegalStateException("Writer is not initialized");
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
            String target_name = GeneralMenu.universalInput("Input new file name");
            target_path += "/" + target_name + target_extension;
            fileWriter.write(target_path, player_map);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public void export_DB() throws Exception {
        DBAccess.wipe();
        for(Player player : player_map.values()){
            DBAccess.add(player);
        }
        GeneralMenu.message_popup("Players from map are added to DB");
    }

    public String delete(Integer ID) throws Exception {
        if(player_map == null){
            return "Empty Map";
        }
        if(player_map.containsKey(ID)){
            return "Player does not exist";
        }
        player_map.remove(ID);
        DBAccess.delete(ID);
        setData_changed(true);
        return "Player with ID " + ID + " is deleted";
    }

    public String add(Player player) throws Exception {
        if(isPlayer_Invalid(player)) {
            throw new Exception("Player data is invalid");
        }
        if(player_map == null){
            player_map = new TreeMap<>();
        }
        player_map.put(player.getID(), player);
        DBAccess.add(player);
        setData_changed(true);
        write();
        return "Player with ID " + player.getID() + "is added";
    }

    public String update(Player player) throws Exception {
        if(isPlayer_Invalid(player)) {
            throw new Exception("Player data is invalid");
        }
        player_map.put(player.getID(), player);
        DBAccess.modify(player);
        write();
        return  "Player with ID " + player.getID() + "is modified";
    }

    public String isDBConnected() {
        if(DBAccess.connected()){
            return "DataBase is connected";
        }else{
            return "DataBase is not connected";
        }
    }

    public Player pop(Integer ID) throws Exception {
        if(player_map.containsKey(ID)){
            Player player = player_map.get(ID);
            player_map.remove(ID);
            return player;
        }else{
            throw new Exception("Player does not exist");
        }
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

    public void print_person(){
        if(player_map == null){
            GeneralMenu.message_popup("No player data registered");
        }else{
            for (Player player : player_map.values()) {
                GeneralMenu.message_popup(player.toString());
            }
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

}
