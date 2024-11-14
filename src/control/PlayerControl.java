package control;

import GUI.GeneralDialog;
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

    public void refreshDA() throws Exception {
        playerDA.refresh();
    }

    public String dataSource(){
        String data_source = "";
        if(playerDA.getFilePath() != null){
            String path = playerDA.getFilePath();
            data_source += path.substring(path.lastIndexOf("."));
        }
        return data_source;
    }

    public void createFile() throws OperationException {
        try {
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder());
            playerDA.export();
            playerDA.setDataChanged(true);
        } catch (Exception e) {
            throw new OperationException("Failed to create new file\n" + e.getMessage());
        }
    }

    public void importFile() {
        playerDA.setFilePath(GeneralDataAccess.getPath("file"));
        playerDA.setDBSource(false);
        playerDA.setDataChanged(true);
    }

    public void configureDB(String URL, String port, String database, String user, String password) {
        playerDA.configureDB(URL, port, database, user, password);
    }

    public void configureDB(String URL) {
        playerDA.configureDB(URL);
    }

    public void importDB()  {
        playerDA.setDBSource(true);
        playerDA.setDataChanged(true);
    }

    public boolean connectDB(){
        return playerDA.connectDB();
    }

    public boolean disconnectDB() {
        return playerDA.disconnectDB();
    }

    public boolean DBSource() {
        return playerDA.isDBSource();
    }

    public TreeMap<Integer,Player> getMap(){
        return playerDA.getPlayerMap();
    }

    public void modifyPlayer(int selected_player_id) throws Exception {
        Player player = playerDA.getPlayerMap().get(selected_player_id);
        switch(PlayerDialog.get().selectionDialog("modify_player")){
            // After changing region the server has to be changed too.
            case 0: player.setRegion(PlayerDialog.get().selectionDialog("region_menu",playerDA.getRegionList()));
            case 1: player.setServer(PlayerDialog.get().selectionDialog("server_menu",playerDA.getServerList(player.getRegion())));
                break;
            case 2: player.setName(PlayerDialog.get().input("player_name"));
                break;
            case 3:
                player.setRegion(PlayerDialog.get().selectionDialog("region_menu",playerDA.getRegionList()));
                player.setServer(PlayerDialog.get().selectionDialog("server_menu",playerDA.getServerList(player.getRegion())));
                player.setName(PlayerDialog.get().input("player_name"));
                break;
        }
        playerDA.update(player);
    }

    public void createPlayer() {
        try {
            Player player = new Player();
            player.setRegion(PlayerDialog.get().selectionDialog("region_menu",playerDA.getRegionList()));
            player.setServer(PlayerDialog.get().selectionDialog("server_menu",playerDA.getServerList(player.getRegion())));
            player.setID(createID());
            player.setName(PlayerDialog.get().input("player_name"));
            playerDA.add(player);
        } catch (Exception e) {
            throw new OperationException("Creating player failed\n" + e.getMessage());
        }
    }

    private int createID() {
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

    public void delete(int selected_player_id) throws Exception {
        playerDA.delete(selected_player_id);
    }

    public void export() {
        try {
            if(playerDA.isEmpty()){
                PlayerDialog.get().popup("player_map_null");
            }else{
                switch (PlayerDialog.get().selectionDialog("export_player")){
                    case 0: playerDA.export(); break;
                    case 1: playerDA.exportDB(); break;
                }
            }
        } catch (Exception e) {
            throw new OperationException("Export failed\n" + e.getMessage());
        }
    }

    public void changeLanguage(){
        String language = switch(GeneralDialog.get().selectionDialog("language")){
            case 0 -> "en";
            case 1 -> "es";
            case 2 -> "cn";
            default ->
                    throw new IllegalStateException("Unexpected value: " + GeneralDialog.get().selectionDialog("language"));
        };
        GeneralDialog.get().setLanguage(language);
        PlayerDialog.get().setLanguage(language);
    }
}
