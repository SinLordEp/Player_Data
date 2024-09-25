package model;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerOperationData extends GeneralOperationData{
    private HashMap<Integer, Player> player_data = null;
    private Map<String, String[]> region_server = null;

    public PlayerOperationData() {
    }

    public HashMap<Integer, Player> getPlayer_data() {
        return player_data;
    }

    public void setPlayer_data(HashMap<Integer, Player> player_data) {
        this.player_data = player_data;
    }

    public Map<String, String[]> getRegion_server() {
        return region_server;
    }

    public void setRegion_server(Map<String, String[]> region_server) {
        this.region_server = region_server;
    }

    public boolean containsKey(int ID){
        return player_data.containsKey(ID);
    }
    public Player getFrom_Map(int ID){
        return player_data.get(ID);
    }
    public void putIn_Map(int ID, Player player){
        player_data.put(ID,  player);
    }

    public String[] getServer(String region){
        return region_server.get(region);
    }

    public void print_person(){
        for(Person temp : player_data.values()){
            JOptionPane.showMessageDialog(null, temp.toString());
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
        if(player_data.containsKey(player.getID())){
            JOptionPane.showMessageDialog(null, "Player ID already exists!");
            return false;
        }

        if(player.getName().isBlank()){
            JOptionPane.showMessageDialog(null, "Player Name is blank");
            return false;
        }
        return true;
    }

}
