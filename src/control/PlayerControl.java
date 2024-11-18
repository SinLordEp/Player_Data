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
    private PlayerUI playerUI;

    @Override
    public void run() {
        playerUI = new PlayerUI(this);
        playerUI.run();
    }

    @Override
    public void setDA(GeneralDataAccess DA) {
        this.playerDA = (PlayerDataAccess) DA;
    }

    @Override
    public void onWindowClosing() {
        save();
        System.exit(0);
    }

    public void createFile() throws OperationException {
        try {
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder());
        } catch (Exception e) {
            GeneralDialog.getDialog().message("Failed to create new file\n" + e.getMessage());
        }
        playerUI.refresh();
    }

    public void importFile() {
        save();
        playerDA.setDataSource(DataSource.FILE);
        playerDA.setFilePath(GeneralDataAccess.getPath("file"));
        playerDA.read();
        playerUI.refresh();
    }

    public void configureDB(String URL, String port, String database, String user, char[] password) {
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
        playerUI.refresh();
    }


    public void DBConnection(){
        if(playerDA.isDBConnected()){
            disconnectDB();
        }else{
            connectDB();
        }
    }

    public void connectDB(){
        playerUI.connecting();
        if(playerUI.hasBlank()){
            GeneralDialog.getDialog().popup("db_field_empty");
            playerUI.disconnected();
            return;
        }
        playerUI.getDBLoginInfo();
        if(playerDA.connectDB()){
            playerUI.connected();
        }else{
            GeneralDialog.getDialog().popup("db_login_failed");
            playerUI.disconnected();
        }
    }

    public void disconnectDB() {
        playerUI.disconnecting();
        if(playerDA.disconnectDB()){
            playerUI.disconnected();
        }else{
            GeneralDialog.getDialog().popup("db_disconnect_failed");
            playerUI.connected();
        }
    }

    public TreeMap<Integer,Player> getMap(){
        return playerDA.getPlayerMap();
    }

    public void modify(int selected_player_id){
        playerDA.modify(selected_player_id);
        playerUI.refresh();
    }

    public void add() {
        playerDA.add();
        playerUI.refresh();
    }

    public void delete(int selected_player_id) {
        playerDA.delete(selected_player_id);
        playerUI.refresh();
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
        playerUI.changeLanguage();
    }

    public DataSource getDataSource() {
        return playerDA.getDataSource();
    }

    public void save(){
        if(!getDataSource().equals(DataSource.NONE)){
            playerDA.save();
        }
    }

    public void comboBoxSQL(String sql_type){
        switch (sql_type){
            case "MySQL" -> playerUI.configureMySQL();
            case "SQLite" -> playerUI.configureSQLite();
        }
    }
}
