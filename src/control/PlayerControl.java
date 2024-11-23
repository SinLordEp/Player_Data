package control;

import GUI.DataBaseLogin;
import GUI.GeneralDialog;
import GUI.Player.PlayerUI;
import GUI.Player.PlayerDialog;
import Interface.GeneralControl;
import data.DataSource;
import Interface.GeneralDataAccess;
import data.PlayerDataAccess;
import data.database.SqlDialect;
import data.file.FileType;
import main.OperationException;
import model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public void setDataSource(DataSource dataSource) {
        playerDA.setDataSource(dataSource);
        logger.debug("Data source is set to {}", dataSource);
    }

    @Override
    public void setSQLDialect(SqlDialect dialect) {
        playerDA.setSQLDialect(dialect);
        logger.debug("SQL dialect is set to {}", dialect);
    }

    @Override
    public void setFileType(FileType fileType) {
        playerDA.setFileType(fileType);
        logger.debug("FileType is set to {}", fileType);
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

    public void importData() {
        logger.debug("Importing data: Saving possible data before changing datasource...");
        save();
        logger.debug("Fetching data source...");
        setDataSource(playerUI.getDataSource());
        switch (playerDA.getDataSource()){
            case FILE -> importFile();
            case DATABASE, HIBERNATE -> importDB();
        }
    }

    public void importFile() {
        logger.debug("Importing data from file: Fetching file type...");
        setFileType(playerUI.getFileType());
        logger.info("Fetching file path...");
        String file_path = GeneralDataAccess.getPath(playerDA.getFileType());
        playerDA.setFilePath(file_path);
        logger.debug("File path set to: {}",file_path);
        logger.info("Reading data from file...");
        playerDA.read();
        logger.info("Data read successfully from file, refreshing UI");
        playerUI.refresh();
        logger.info("Finished importing from file");
    }

    public void importDB()  {
        logger.info("Importing data from database: Fetching SQL dialect...");
        setSQLDialect(playerUI.getSQLDialect());
        connectDB();
        logger.info("Reading data from database...");
        playerDA.read();
        logger.info("Data read successfully from database, refreshing UI");
        playerUI.refresh();
        logger.info("Finished importing from database");
    }

    public void connectDB(){
        logger.debug("Connecting to database...");
        DataBaseLogin dbLogin = new DataBaseLogin(playerDA.getDefaultDatabaseInfo());
        if(!dbLogin.isValid()){
            GeneralDialog.getDialog().popup("db_login_canceled");
            return;
        }
        if(playerDA.connectDB()){
            logger.info("Database connected successfully");
        }else{
            logger.info("Database could not connect");
            GeneralDialog.getDialog().popup("db_login_failed");
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

    public void save(){
        logger.info("Saving data: Fetching current data source...");
        DataSource dataSource = playerDA.getDataSource();
        logger.info("Current data source is: {}", dataSource.toString());
        if(!dataSource.equals(DataSource.NONE)){
            logger.debug("Trigger saving procedure: calling DA to save");
            playerDA.save();
            logger.debug("Finished to save data");
            return;
        }
        logger.info("Current data source is NONE, returning...");
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

}
