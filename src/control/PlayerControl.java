package control;

import GUI.GeneralMenu;
import GUI.Player.PlayerUI;
import GUI.Player.PlayerMenu;
import data.GeneralDataAccess;
import data.PlayerDataAccess;
import model.Player;
import java.util.TreeMap;

public class PlayerControl implements GeneralControl {
    private PlayerDataAccess playerDA;
    @Override
    public void run() {
        PlayerUI playerUI = new PlayerUI(this);
        playerUI.run();
        System.out.println("Operation UI is running");
    }

    @Override
    public void setDA(GeneralDataAccess DA) {
        this.playerDA = (PlayerDataAccess) DA;
    }

    public void refresh_DA() throws Exception {
        playerDA.refresh();
    }

    public String data_source(String SQL_Type){
        String data_source = "Data Source: ";
        if(playerDA.DB_source()){
            data_source += SQL_Type;
        }else if(playerDA.getFile_path() != null){
            String path = playerDA.getFile_path();
            data_source += path.substring(path.lastIndexOf(".")) + " File";
        }else{
            data_source += "null";
        }
        return data_source;
    }

    public void create_file() throws Exception {
        playerDA.setFile_path(GeneralDataAccess.new_path_builder());
        playerDA.write();
        playerDA.setData_changed(true);
    }

    public void import_file() {
        playerDA.setFile_path(GeneralDataAccess.get_path("file"));
        playerDA.setDB_source(false);
        playerDA.setData_changed(true);
    }

    public void configure_db(String URL, String port, String database, String user, String password) {
        playerDA.configure_db(URL, port, database, user, password);
    }

    public void configure_db(String URL) {
        playerDA.configure_db(URL);
    }

    public void import_db()  {
        playerDA.setDB_source(true);
        playerDA.setData_changed(true);
    }

    public boolean connect_db(){
        return playerDA.connect_db();
    }

    public boolean disconnect_db() throws Exception {
        return playerDA.disconnect_db();
    }

    public boolean DB_source() {
        return playerDA.DB_source();
    }

    public TreeMap<Integer,Player> getMap(){
        return playerDA.getPlayer_map();
    }

    public void modify_player_control(int selected_player_id) throws Exception {
        Player player = playerDA.getPlayer_map().get(selected_player_id);
        switch(PlayerMenu.modify_player_menu()){
            // After changing region the server has to be changed too.
            case "Region": player.setRegion(PlayerMenu.region_chooser(playerDA.getRegion_list()));
            case "Server": player.setServer(PlayerMenu.server_chooser(playerDA.getServer_list(player.getRegion()))); break;
            case "Name": player.setName(GeneralMenu.universalInput("Enter player name: ")); break;
            case "ALL":
                player.setRegion(PlayerMenu.region_chooser(playerDA.getRegion_list()));
                player.setServer(PlayerMenu.server_chooser(playerDA.getServer_list(player.getRegion())));
                player.setName(GeneralMenu.universalInput("Enter player name: "));
                break;
        }
        GeneralMenu.message_popup(playerDA.update(player));
    }

    public void create_player_control() throws Exception {
        Player player = new Player();
        player.setRegion(PlayerMenu.region_chooser(playerDA.getRegion_list()));
        player.setServer(PlayerMenu.server_chooser(playerDA.getServer_list(player.getRegion())));
        player.setID(create_ID_control());
        player.setName(GeneralMenu.universalInput("Enter player name: "));
        GeneralMenu.message_popup(playerDA.add(player));
    }

    private int create_ID_control() throws Exception {
        while (true) {
            try {
                int ID = PlayerMenu.ID_input_UI();
                if (playerDA.containsKey(ID)) {
                    throw new Exception("ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                PlayerMenu.error_message("ID Format");
            }
        }
    }

    public void delete_control(int selected_player_id){
        GeneralMenu.message_popup(playerDA.delete(selected_player_id));
    }

    public void export_control() throws Exception {
        if(playerDA.isEmpty()){
            PlayerMenu.error_message("Empty Map");
        }else{
            switch (PlayerMenu.export_menu()){
                case "Export to File": playerDA.export(); break;
                case "Overwrite Database": playerDA.export_DB(); break;
            }
        }
    }


}
