package control;

import GUI.Player.PlayerUI;
import GUI.Player.PlayerDialog;
import Interface.GeneralControl;
import data.GeneralDataAccess;
import data.PlayerDataAccess;
import main.OperationException;
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
        String data_source = PlayerDialog.get().get_text("data_source");
        if(playerDA.DB_source()){
            data_source += SQL_Type;
        }else if(playerDA.getFile_path() != null){
            String path = playerDA.getFile_path();
            data_source += path.substring(path.lastIndexOf("."));
        }else{
            data_source += "null";
        }
        return data_source;
    }

    public void create_file() throws OperationException {
        try {
            playerDA.setFile_path(GeneralDataAccess.new_path_builder());
            playerDA.write();
            playerDA.setData_changed(true);
        } catch (Exception e) {
            throw new OperationException("Failed to create new file\n" + e.getMessage());
        }
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

    public boolean disconnect_db() {
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
        switch(PlayerDialog.get().selectionDialog("modify_player")){
            // After changing region the server has to be changed too.
            case 0: player.setRegion(PlayerDialog.get().selectionDialog("region_menu",playerDA.getRegion_list()));
            case 1: player.setServer(PlayerDialog.get().selectionDialog("server_menu",playerDA.getServer_list(player.getRegion())));
                break;
            case 2: player.setName(PlayerDialog.get().input("player_name"));
                break;
            case 3:
                player.setRegion(PlayerDialog.get().selectionDialog("region_menu",playerDA.getRegion_list()));
                player.setServer(PlayerDialog.get().selectionDialog("server_menu",playerDA.getServer_list(player.getRegion())));
                player.setName(PlayerDialog.get().input("player_name"));
                break;
        }
        playerDA.update(player);
    }

    public void create_player_control() {
        try {
            Player player = new Player();
            player.setRegion(PlayerDialog.get().selectionDialog("region_menu",playerDA.getRegion_list()));
            player.setServer(PlayerDialog.get().selectionDialog("server_menu",playerDA.getServer_list(player.getRegion())));
            player.setID(create_ID_control());
            player.setName(PlayerDialog.get().input("player_name"));
            playerDA.add(player);
        } catch (Exception e) {
            throw new OperationException("Creating player failed\n" + e.getMessage());
        }
    }

    private int create_ID_control() {
        while (true) {
            try {
                int ID = Integer.parseInt(PlayerDialog.get().input("id"));
                if (playerDA.containsKey(ID)) {
                    throw new OperationException("ID already existed\n");
                } else return ID;
            } catch (NumberFormatException e) {
                PlayerDialog.get().popup("number_format_invalid");
            }
        }
    }

    public void delete_control(int selected_player_id){
        playerDA.delete(selected_player_id);
    }

    public void export_control() {
        try {
            if(playerDA.isEmpty()){
                PlayerDialog.get().popup("player_map_null");
            }else{
                switch (PlayerDialog.get().selectionDialog("export_player")){
                    case 0: playerDA.export(); break;
                    case 1: playerDA.export_DB(); break;
                }
            }
        } catch (Exception e) {
            throw new OperationException("Export failed\n" + e.getMessage());
        }
    }

}
