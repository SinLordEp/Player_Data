package control;

import GUI.GeneralDialog;
import GUI.Player.PlayerUI;
import GUI.Player.PlayerDialog;
import Interface.GeneralControl;
import data.DataSource;
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

    public void createFile() throws OperationException {
        try {
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder());
        } catch (Exception e) {
            GeneralDialog.getDialog().message("Failed to create new file\n" + e.getMessage());
        }
    }

    public void importFile() {
        save();
        playerDA.setDataSource(DataSource.FILE);
        playerDA.setFilePath(GeneralDataAccess.getPath("file"));
        playerDA.read();
    }

    public void configureDB(String URL, String port, String database, String user, String password) {
        playerDA.configureDB(URL, port, database, user, password);
    }

    public void configureDB(String URL) {
        playerDA.configureDB(URL);
    }

    public void importDB(String database_type)  {
        save();
        switch(database_type){
            case "MySQL" -> playerDA.setDataSource(DataSource.MYSQL);
            case "SQLite" -> playerDA.setDataSource(DataSource.SQLITE);
        }
        playerDA.read();
    }

    public boolean connectDB(){
        return playerDA.connectDB();
    }

    public boolean disconnectDB() {
        return playerDA.disconnectDB();
    }

    public TreeMap<Integer,Player> getMap(){
        return playerDA.getPlayerMap();
    }

    public void modify(int selected_player_id){
        playerDA.modify(selected_player_id);
    }

    public void add() {
        playerDA.add();
    }


    public void delete(int selected_player_id) {
        playerDA.delete(selected_player_id);
    }

    public void export() {
        try {
            if(playerDA.isEmpty()){
                PlayerDialog.getDialog().popup("player_map_null");
            }else{
                switch (PlayerDialog.getDialog().selectionDialog("export_player")){
                    case 0: playerDA.export(); break;
                    case 1: playerDA.exportDB(); break;
                }
            }
        } catch (Exception e) {
            GeneralDialog.getDialog().message("Failed to export data\n" + e.getMessage());
        }
    }

    public void changeLanguage(){
        String language = switch(GeneralDialog.getDialog().selectionDialog("language")){
            case 0 -> "en";
            case 1 -> "es";
            case 2 -> "cn";
            default ->
                    throw new IllegalStateException("Unexpected value: " + GeneralDialog.getDialog().selectionDialog("language"));
        };
        GeneralDialog.getDialog().setLanguage(language);
        PlayerDialog.getDialog().setLanguage(language);
    }

    public DataSource getDataSource() {
        return playerDA.getDataSource();
    }

    public void save(){
        if(!getDataSource().equals(DataSource.NONE)){
            playerDA.save();
        }
    }
}
