package control;

import GUI.DataBaseLogin;
import GUI.DataSourceChooser;
import GUI.GeneralDialog;
import GUI.Player.PlayerDialog;
import GUI.Player.PlayerModify;
import GUI.Player.PlayerUI;
import Interface.EventListener;
import Interface.GeneralControl;
import Interface.GeneralDataAccess;
import data.DataSource;
import data.PlayerDataAccess;
import data.database.SqlDialect;
import data.file.FileType;
import exceptions.*;
import model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;


public class PlayerControl implements GeneralControl {
    private static final Logger logger = LoggerFactory.getLogger(PlayerControl.class);
    private PlayerDataAccess playerDA;
    private final List<EventListener<SortedMap<?,?>>> listeners = new ArrayList<>();

    @Override
    public void run() {
        try {
            playerDA.initializeRegionServer();
        } catch (FileManageException e) {
            PlayerDialog.getDialog().popup("region_server_null");
            return;
        }
        PlayerUI playerUI = new PlayerUI(this);
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
        logger.debug("Setting new data source...");
        playerDA.setDataSource(dataSource);
        logger.debug("Data source is set to {}", dataSource);
    }

    public void createFile() {
        logger.debug("Creating file: Fetching data source...");
        try {
            DataSourceChooser dataSourceChooser = new DataSourceChooser(DataSource.FILE);
            if(dataSourceChooser.isCancelled()){
                throw new OperationCancelledException();
            }
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder((FileType) dataSourceChooser.getDataType()));
            logger.info("Path built successfully");
            playerDA.createNewFile();
        } catch (OperationCancelledException e) {
            logger.error("Failed to create new file, Cause: Operation cancelled");
            GeneralDialog.getDialog().popup("operation_cancelled");
            return;
        } catch (FileManageException e) {
            logger.error("Failed to create new file, Cause: ", e);
        }
        logger.info("File created successfully");
    }

    public void importData() {
        try {
            logger.debug("Importing data: Saving possible data before changing datasource...");
            save();
            logger.info("Fetching data source and data type");
            DataSourceChooser dataSourceChooser = new DataSourceChooser();
            if(dataSourceChooser.isCancelled()){
                throw new OperationCancelledException();
            }
            playerDA.setDataSource(dataSourceChooser.getDataSource());
            Object dataType = dataSourceChooser.getDataType();
            switch (dataType){
                case FileType ignore -> playerDA.setFileType((FileType) dataType);
                case SqlDialect ignore -> playerDA.setSQLDialect((SqlDialect) dataType);
                default -> throw new DataTypeException("Unexpected value: " + dataType);
            }
            switch (playerDA.getDataSource()){
                case FILE -> importFile();
                case DATABASE, HIBERNATE -> importDB();
            }
            notifyListeners("dataSource_set",null);
        } catch (OperationCancelledException e) {
            GeneralDialog.getDialog().popup("operation_cancelled");
        } catch (DataTypeException e){
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
        }
    }

    public void importFile() {
        logger.debug("Importing data from file: Fetching file path...");
        String file_path;
        try {
            file_path = GeneralDataAccess.getPath(playerDA.getFileType());
            playerDA.setFilePath(file_path);
            logger.debug("File path set to: {}",file_path);
            logger.info("Reading data from file...");
            playerDA.read();
            logger.info("Data read successfully from file, refreshing UI");
            notifyListeners("data_changed", playerDA.getPlayerMap());
            logger.info("Finished importing from file");
        } catch (OperationCancelledException e) {
            GeneralDialog.getDialog().popup("operation_cancelled");
        }
    }

    public void importDB()  {
        logger.info("Importing data from database: ");
        if(connectDB()){
            logger.info("Reading data from database...");
            playerDA.read();
            logger.info("Data read successfully from database, refreshing UI");
            notifyListeners("data_changed", playerDA.getPlayerMap());
            logger.info("Finished importing from database");
            playerDA.disconnectDB();
            logger.info("Disconnected from database to release resources");
        }
    }

    @Override
    public boolean connectDB(){
        logger.debug("Connecting to database...");
        try {
            HashMap<String, String> login_info = playerDA.getDefaultDatabaseInfo(playerDA.getSQLDialect());
            DataBaseLogin dbLogin = new DataBaseLogin(login_info);
            if(!dbLogin.isValid()){
                logger.info("Connecting to database cancelled: User cancelled operation");
                GeneralDialog.getDialog().popup("db_login_cancelled");
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
        } catch (ConfigErrorException e) {
            logger.error(e.getMessage());
            GeneralDialog.getDialog().popup("config_error");
            return false;
        } catch (DatabaseException e) {
            logger.error(e.getMessage());
            GeneralDialog.getDialog().popup("db_login_failed");
            return false;
        } catch (OperationCancelledException e) {
            logger.info("Operation cancelled");
            GeneralDialog.getDialog().popup("operation_cancelled");
            return false;
        }
    }

    public void modify(int selected_player_id){
        try {
            logger.info("Modifying player with ID: {}", selected_player_id);
            PlayerModify playerModify = new PlayerModify(playerDA.getRegion_server_map(), playerDA.getPlayer(selected_player_id));
            if(playerModify.isCancelled()){
                throw new OperationCancelledException();
            }
            playerDA.modify(playerModify.getPlayer());
            notifyListeners("data_changed", playerDA.getPlayerMap());
            logger.info("Finished updating player with ID: {}", selected_player_id);
            PlayerDialog.getDialog().popup("modified_player");
        } catch (OperationCancelledException e) {
            logger.info("Modify operation cancelled");
            GeneralDialog.getDialog().popup("operation_cancelled");
        }
    }

    public void add() {
        try {
            logger.info("Adding player...");
            PlayerModify playerModify = new PlayerModify(playerDA.getRegion_server_map(), playerDA.getPlayerMap().keySet(), new Player());
            if(playerModify.isCancelled()){
                throw new OperationCancelledException();
            }
            playerDA.add(playerModify.getPlayer());
            notifyListeners("data_changed", playerDA.getPlayerMap());
            logger.info("Finished adding player.");
            PlayerDialog.getDialog().popup( "added_player");
        } catch (OperationCancelledException e) {
            logger.info("Adding player operation cancelled");
            GeneralDialog.getDialog().popup("operation_cancelled");
        }
    }

    public void delete(int selected_player_id) {
        logger.info("Deleting player with ID: {}", selected_player_id);
        playerDA.delete(selected_player_id);
        notifyListeners("data_changed", playerDA.getPlayerMap());
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
            DataSourceChooser dataSourceChooser = new DataSourceChooser();
            DataSource dataSource = dataSourceChooser.getDataSource();
            switch (dataSource){
                case FILE -> exportFile((FileType) dataSourceChooser.getDataType());
                case DATABASE, HIBERNATE -> exportDB(dataSource, (SqlDialect) dataSourceChooser.getDataType());
            }
            logger.info("Finished exporting data.");
        } catch (OperationException e){
            logger.error(e.getMessage());
            GeneralDialog.getDialog().popup("export_failed");
        } catch (Exception e) {
            logger.error(e.getMessage());
            GeneralDialog.getDialog().popup("unknown_error");
        }
    }

    private void exportFile(FileType fileType) {
        logger.info("Exporting data to file...");
        playerDA.setFileType(fileType);
        try {
            playerDA.export();
        } catch (Exception e) {
            throw new OperationException("export_file");
        }
    }

    private void exportDB(DataSource target_source, SqlDialect target_dialect) {
        logger.info("Exporting data to database...");
        try {
            //Fetching default login info
            HashMap<String, String> login_info = playerDA.getDefaultDatabaseInfo(target_dialect);
            DataBaseLogin dbLogin = new DataBaseLogin(login_info);
            if(!dbLogin.isValid()){
                logger.info("Exporting to database cancelled: User cancelled operation");
                GeneralDialog.getDialog().popup("db_login_cancelled");
                return;
            }
            playerDA.exportDB(target_source, target_dialect, login_info);
        } catch (ConfigErrorException e) {
            logger.error(e.getMessage());
        } catch (DatabaseException e) {
            logger.error(e.getMessage());
            throw new OperationException("export_db");
        }
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
        try {
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
        } catch (DatabaseException e) {
            logger.error(e.getMessage());
            GeneralDialog.getDialog().popup("config_error");
        }
    }

    public void changeLanguage(){
        logger.info("Changing language...");
        String language;
        try {
            language = switch(GeneralDialog.getDialog().selectionDialog("language")){
                case 0 -> "en";
                case 1 -> "es";
                case 2 -> "cn";
                default -> throw new IllegalStateException("Unexpected value: " + GeneralDialog.getDialog().selectionDialog("language"));
            };
        } catch (OperationCancelledException e) {
            logger.info("Language changing operation cancelled");
            GeneralDialog.getDialog().popup("operation_cancelled");
            return;
        }
        GeneralDialog.getDialog().setLanguage(language);
        PlayerDialog.getDialog().setLanguage(language);
        notifyListeners("language_changed",null);
        logger.info("Finished changing language to: {}", language);
    }

    public void addListener(EventListener<SortedMap<?,?>> listener){
        listeners.add(listener);
    }

    private void notifyListeners(String event, SortedMap<?,?> data){
        for(EventListener<SortedMap<?,?>> listener : listeners){
            listener.onEvent(event, data);
        }
    }


}
