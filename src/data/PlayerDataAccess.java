package data;

import GUI.GeneralMenu;
import model.Player;
import file.PlayerReader;
import file.PlayerWriter;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerDataAccess extends GeneralDataAccess<Map<?,?>> {
    private HashMap<Integer, Player> player_data = null;
    private HashMap<String, String[]> region_server = null;
    private String[] region_list;
    public PlayerDataAccess() {
        super.setReader(new PlayerReader());
        super.setWriter(new PlayerWriter());
    }

    @SuppressWarnings("unchecked")
    public void read() throws Exception {
        if(region_server == null) {
            region_server = PlayerReader.read_region_server();
            region_list = region_server.keySet().toArray(new String[0]);
            GeneralMenu.message_popup("Region and server loaded");
        }

        if(reader != null){
            player_data = (HashMap<Integer, Player>) reader.read(file_path);
        }
    }

    public void write() throws Exception {
        if(writer != null){
            writer.write(file_path, player_data);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public void export() throws Exception {
        if(writer != null){
            String target_path = get_path("path");
            String target_extension = choose_extension();
            String target_name = GeneralMenu.universalInput("Input new file name");
            target_path += "/" + target_name + target_extension;
            writer.write(target_path, player_data);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public HashMap<Integer, Player> getPlayer_data() {
        return player_data;
    }

    public void setPlayer_data(HashMap<Integer, Player> player_data) {
        this.player_data = player_data;
    }

    public String[] getRegion_list() {
        return region_list;
    }

    public String[] getServer_list(String region){
        return region_server.get(region);
    }

    public boolean containsKey(int ID){
        if(player_data == null){
            return false;
        }else{
            return player_data.containsKey(ID);
        }
    }

    public Player getFrom_Map(int ID){
        return player_data.get(ID);
    }


    public void print_person(){
        if(player_data == null){
            GeneralMenu.message_popup("No player data registered");
        }else{
            for (Player player : player_data.values()) {
                GeneralMenu.message_popup(player.toString());
            }
        }
    }

    public boolean isPlayer_Valid(Player player){
        if(region_server == null){
            JOptionPane.showMessageDialog(null, "Region server is null!");
            return false;
        }
        if(!region_server.containsKey(player.getRegion())){
            JOptionPane.showMessageDialog(null, "Player Region doesn't exist!");
            return false;
        }
        boolean server_valid = false;
        for(String server : region_server.get(player.getRegion())){
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

        if(player_data == null) return true;

        if(player.getName().isBlank()){
            JOptionPane.showMessageDialog(null, "Player Name is blank");
            return false;
        }
        return true;
    }

    public boolean isPlayerMap_Valid(){
        if(player_data == null){
            GeneralMenu.message_popup("No player data registered");
            return true;
        }
        for(Player player : player_data.values()){
            if(!isPlayer_Valid(player)){
                return false;
            }
        }
        return true;
    }

    public void update_changes() throws Exception {
        if(isFile_changed()) {
            read();
            setFile_changed(false);
        }
    }
}
