package control;

import GUI.GeneralDialog;
import GUI.Player.PlayerUI;
import GUI.Player.PlayerDialog;
import Interface.GeneralControl;
import data.DataSource;
import Interface.GeneralDataAccess;
import data.PlayerDataAccess;
import data.database.SqlDialect;
import main.OperationException;
import model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.TreeMap;

public class PlayerControl implements GeneralControl {
    private static final Logger logger = LoggerFactory.getLogger(PlayerControl.class);
    private PlayerDataAccess playerDA;
    private PlayerUI playerUI;

    @Override
    public void run() {
        playerUI = new PlayerUI(this);
        logger.debug("Trying to build player frame");
        playerUI.run();
        logger.info("Finished building player frame");
    }

    @Override
    public void setDA(GeneralDataAccess DA) {
        this.playerDA = (PlayerDataAccess) DA;
    }

    @Override
    public void onWindowClosing() {
        logger.debug("Trigger Player window closing procedure...");
        save();
        logger.info("Shutting down");
        System.exit(0);
    }

    @Override
    public HashMap<String, String> getDefaultDatabase() {
        return playerDA.getDefaultDatabaseInfo();
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        save();
        playerDA.setDataSource(dataSource);
    }

    @Override
    public void setSQLDialect(SqlDialect dialect) {
        save();
        playerDA.setSqlDialect(dialect);
    }

    public void createFile() throws OperationException {
        logger.debug("Creating file by building path");
        try {
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder());
            logger.info("Path built successfully");
        } catch (Exception e) {
            logger.error("Failed to create new file, Cause: {}", e.getMessage());
            GeneralDialog.getDialog().message("Failed to create new file\n" + e.getMessage());
        }
        playerUI.refresh();
        logger.info("File created successfully");
    }

    //todo
    public void importData() {
        logger.debug("Importing from file...");
        playerDA.setDataSource(playerUI.getDataSource());
        if(playerDA.getDataSource().equals(DataSource.DATABASE) || playerDA.getDataSource().equals(DataSource.HIBERNATE)) {
            playerDA.setSqlDialect(playerUI.getSQLDialect());
        }
        logger.info("Changing data source to FILE.");
    }

    public void importFile() {

        String file_path = GeneralDataAccess.getPath("file");
        logger.debug("File path set to: {}",file_path);
        playerDA.setFilePath(file_path);

        logger.info("Reading data from file...");
        playerDA.read();
        logger.info("Data read successfully from file.");

        playerUI.refresh();
        logger.info("Finished importing from file");
    }

    /*public void importDB(String database_type)  {
        logger.info("Importing to database, trigger save procedure");
        save();
        logger.info("Changing data source to corresponding database type then read data");
        switch(database_type){
            case "MySQL" -> playerDA.setDataSource(DataSource.MYSQL);
            case "SQLite" -> playerDA.setDataSource(DataSource.SQLITE);
        }
        playerDA.read();
        playerUI.refresh();
        logger.info("Finished importing from database");
    }*/

    public void DBConnection(){
        if(playerDA.isDBConnected()){
            disconnectDB();
        }else{
            connectDB();
        }
    }

    public void connectDB(){
        logger.debug("Trying to connect to database...");
        playerUI.connecting();
        logger.info("Configuring database info by user input");

        //playerUI.setDBLoginInfo();

        logger.debug("Connecting database...");
        if(playerDA.connectDB()){
            playerUI.connected();
            logger.info("Database connected successfully");
        }else{
            logger.info("Database could not connect");
            GeneralDialog.getDialog().popup("db_login_failed");
            playerUI.disconnected();
        }
    }

    public void disconnectDB() {
        logger.debug("Trying to disconnect from database...");
        playerUI.disconnecting();
        if(playerDA.disconnectDB()){
            playerUI.disconnected();
            logger.info("Database disconnected successfully");
        }else{
            logger.info("Database could not disconnect");
            GeneralDialog.getDialog().popup("db_disconnect_failed");
            playerUI.connected();
        }
    }

    public TreeMap<Integer,Player> getMap(){
        return playerDA.getPlayerMap();
    }

    public void modify(int selected_player_id){
        logger.info("Modifying player with ID: {}", selected_player_id);
        playerDA.modify(selected_player_id);
        playerUI.refresh();
        logger.info("Finished updating player with ID: {}", selected_player_id);
    }

    public void add() {
        logger.info("Adding player...");
        playerDA.add();
        playerUI.refresh();
        logger.info("Finished adding player.");
    }

    public void delete(int selected_player_id) {
        logger.info("Deleting player with ID: {}", selected_player_id);
        playerDA.delete(selected_player_id);
        playerUI.refresh();
        logger.info("Finished deleting player with ID: {}", selected_player_id);
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
        logger.info("Changing language...");
        String language = switch(GeneralDialog.getDialog().selectionDialog("language")){
            case 0 -> "en";
            case 1 -> "es";
            case 2 -> "cn";
            default ->
                    throw new IllegalStateException("Unexpected value: " + GeneralDialog.getDialog().selectionDialog("language"));
        };
        logger.info("Language set to: {}", language);
        GeneralDialog.getDialog().setLanguage(language);
        PlayerDialog.getDialog().setLanguage(language);
        playerUI.changeLanguage(playerDA.isDBConnected());
        logger.info("Finished changing language to: {}", language);
    }

    public DataSource getDataSource() {
        return playerDA.getDataSource();
    }

    public void save(){
        logger.info("Trying to save data, Fetching data source...");
        DataSource dataSource = playerDA.getDataSource();
        logger.info("Data source is set to: {}", dataSource.toString());
        if(!dataSource.equals(DataSource.NONE)){
            logger.debug("Trigger DataAccess saving procedure...");
            playerDA.save();
        }
        logger.debug("Finished to save data");
    }
}
