package data;

import DB.PlayerDBA;
import GUI.GeneralMenu;
import model.Player;
import file.PlayerFileReader;
import file.PlayerFileWriter;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataAccess extends GeneralDataAccess<Map<?,?>, Player, Integer> {
    private HashMap<Integer, Player> player_map = null;
    private final HashMap<String, String[]> region_server_map;
    private final String[] region_list;
    public PlayerDataAccess() throws Exception {
        reader = new PlayerFileReader();
        writer = new PlayerFileWriter();
        DBAccess = new PlayerDBA();
        region_server_map = PlayerFileReader.read_region_server();
        region_list = region_server_map.keySet().toArray(new String[0]);
        DBAccess.initialize();
    }

    @SuppressWarnings("unchecked")
    public void read() throws Exception {
        if(reader != null && file_path != null){
            player_map = (HashMap<Integer, Player>) reader.read(file_path);
        }else{
            player_map = (HashMap<Integer, Player>) DBAccess.read();
            isDBSource = true;
        }
    }

    public void write() throws Exception {
        if(writer != null && !isDBSource){
            writer.write(file_path, player_map);
            setFile_changed(true);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public void refresh() throws Exception {
        if(isFile_changed()) {
            read();
            if(!isPlayerMap_Valid()){
                throw new Exception("Player data corrupted");
            }
            setFile_changed(false);
        }
    }

    public void export() throws Exception {
        if(writer != null){
            String target_extension = choose_extension();
            String target_path = get_path("path");
            String target_name = GeneralMenu.universalInput("Input new file name");
            target_path += "/" + target_name + target_extension;
            writer.write(target_path, player_map);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    @SuppressWarnings("unchecked")
    public void export_DB() throws Exception {
        DBAccess.wipe();
        for(Player player : player_map.values()){
            DBAccess.add(player);
        }
    }

    @SuppressWarnings("unchecked")
    public String delete(Integer ID) throws Exception {
        if(player_map == null){
            return "Empty Map";
        }
        if(player_map.containsKey(ID)){
            return "Player does not exist";
        }
        player_map.remove(ID);
        setFile_changed(true);
        DBAccess.delete(ID);
        return "Player with ID " + ID + " is deleted";
    }

    public String add(Player player) throws Exception {
        if(isPlayer_Valid(player)) {
            if(player_map == null){
                player_map = new HashMap<>();
            }
            player_map.put(player.getID(), player);
            write();
        }else{
            throw new Exception("Player data is invalid");
        }
        return "Player with ID " + player.getID() + "is added";
    }

    @SuppressWarnings("unchecked")
    public String update(Player player) throws Exception {
        if(isPlayer_Valid(player)) {
            player_map.put(player.getID(), player);
            DBAccess.modify(player);
            write();
        }
        return  "Player with ID " + player.getID() + "is modified";
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

    public HashMap<Integer, Player> getPlayer_map() {
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

    public boolean isPlayer_Valid(Player player){
        if(region_server_map == null){
            JOptionPane.showMessageDialog(null, "Region server is null!");
            return false;
        }
        if(!region_server_map.containsKey(player.getRegion())){
            JOptionPane.showMessageDialog(null, "Player Region doesn't exist!");
            return false;
        }
        boolean server_valid = false;
        for(String server : region_server_map.get(player.getRegion())){
            if (server.equals(player.getServer())) {
                server_valid = true;
                break;
            }
        }
        if(!server_valid){
            JOptionPane.showMessageDialog(null, "Player Server doesn't exist!");
            return false;
        }

        if(player.getID() <= 0){
            JOptionPane.showMessageDialog(null, "Player ID is ilegal");
            return false;
        }

        if(player_map == null) return true;

        if(player.getName().isBlank()){
            JOptionPane.showMessageDialog(null, "Player Name is blank");
            return false;
        }
        return true;
    }

    public boolean isPlayerMap_Valid(){
        if(player_map == null){
            GeneralMenu.message_popup("No player data registered");
            return true;
        }
        for(Player player : player_map.values()){
            if(!isPlayer_Valid(player)){
                return false;
            }
        }
        return true;
    }

}
