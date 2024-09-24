package model;

import javax.swing.*;
import java.util.Map;

public class PlayerOperationData extends GeneralOperationData{
    private Map<Integer, Player> player_data = null;
    private Map<String, String[]> region_server = null;
    private String person_type = "";

    public PlayerOperationData() {
        super.setData_class("Person");
    }

    public Map<Integer, Player> getPlayer_data() {
        return player_data;
    }

    public void setPlayer_data(Map<Integer, Player> player_data) {
        this.player_data = player_data;
    }

    public Map<String, String[]> getRegion_server() {
        return region_server;
    }

    public void setRegion_server(Map<String, String[]> region_server) {
        this.region_server = region_server;
    }

    public String getPerson_type() {
        return person_type;
    }

    public void setPerson_type(String person_type) {
        this.person_type = person_type;
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
}
