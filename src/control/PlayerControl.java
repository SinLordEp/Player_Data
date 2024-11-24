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

    //todo:连接新增的两个combo,不再使用弹窗选择
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
        logger.debug("Fetching new data source...");
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
        if(connectDB()){
            logger.info("Reading data from database...");
            playerDA.read();
            logger.info("Data read successfully from database, refreshing UI");
            playerUI.refresh();
            logger.info("Finished importing from database");
            playerDA.disconnectDB();
            logger.info("Disconnected from database to release resources");
        }
    }

    public boolean connectDB(){
        logger.debug("Connecting to database...");
        HashMap<String, String> login_info = playerDA.getDefaultDatabaseInfo(playerDA.getSQLDialect());
        DataBaseLogin dbLogin = new DataBaseLogin(login_info);
        if(!dbLogin.isValid()){
            logger.info("Connecting to database cancelled: User cancelled operation");
            GeneralDialog.getDialog().popup("db_login_canceled");
            return false;
        }
        playerDA.setLogin_info(login_info);
        if(playerDA.connectDB()){
            logger.info("Database connected successfully");
            return true;
        }else{
            logger.error("Database could not connect");
            GeneralDialog.getDialog().popup("db_login_failed");
            return false;
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
            logger.info("Exporting data: Checking if data exists...");
            if(playerDA.isEmpty()){
                logger.info("Exporting data cancelled: No player data found");
                PlayerDialog.getDialog().popup("player_map_null");
                return;
            }
            switch (PlayerDialog.getDialog().selectionDialog("export_player")){
                case 0:
                    logger.info("Exporting data to file...");
                    playerDA.export();
                    break;
                case 1:
                    logger.info("Exporting data to database...");
                    exportDB();
                    break;
            }
            logger.info("Finished exporting data.");
        } catch (Exception e) {
            GeneralDialog.getDialog().message("Failed to export data\n" + e.getMessage());
        }
    }

    private void exportDB() {
        //Building data sources dialog options
        DataSource[] dataSources = DataSource.values();
        DataSource[] usable_dataSources = new DataSource[dataSources.length-2];
        System.arraycopy(dataSources, 2, usable_dataSources, 0, usable_dataSources.length);
        DataSource target_source = (DataSource) GeneralDialog.getDialog().selectionDialog("target_source",usable_dataSources);
        //Building SQL dialect dialog options
        SqlDialect[] dialects = SqlDialect.values();
        SqlDialect[] usable_dialects = new SqlDialect[dialects.length-1];
        System.arraycopy(dialects, 1, usable_dialects, 0, usable_dialects.length);
        SqlDialect target_dialect = (SqlDialect) GeneralDialog.getDialog().selectionDialog("target_dialect",usable_dialects);
        //Fetching default login info
        HashMap<String, String> login_info = playerDA.getDefaultDatabaseInfo(target_dialect);
        DataBaseLogin dbLogin = new DataBaseLogin(login_info);
        if(!dbLogin.isValid()){
            logger.info("Exporting to database cancelled: User cancelled operation");
            GeneralDialog.getDialog().popup("db_login_canceled");
            return;
        }
        playerDA.exportDB(target_source, target_dialect, login_info);
        PlayerDialog.getDialog().popup("exported_db");
    }

    public void save(){
        logger.info("Saving data: Checking if data exists...");
        if(playerDA.isEmpty()){
            logger.info("Saving data cancelled: No player data found");
            return;
        }
        logger.info("Saving data: Fetching current data source...");
        DataSource dataSource = playerDA.getDataSource();
        logger.info("Saving data to current data source: {}", dataSource.toString());
        switch (dataSource){
            case NONE:
                logger.info("Current data source is NONE, returning...");
                return;
            case FILE:
                playerDA.save();
                break;
            case DATABASE, HIBERNATE:
                playerDA.connectDB();
                playerDA.save();
                playerDA.disconnectDB();
                break;
        }
        logger.debug("Data saved successfully");
    }

    public void changeLanguage(){
        logger.info("Changing language...");
        String language = switch(GeneralDialog.getDialog().selectionDialog("language")){
            case 0 -> "en";
            case 1 -> "es";
            case 2 -> "cn";
            default -> throw new IllegalStateException("Unexpected value: " + GeneralDialog.getDialog().selectionDialog("language"));
        };
        GeneralDialog.getDialog().setLanguage(language);
        PlayerDialog.getDialog().setLanguage(language);
        playerUI.changeLanguage();
        logger.info("Finished changing language to: {}", language);
    }

}
