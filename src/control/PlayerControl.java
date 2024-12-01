package control;

import GUI.DataSourceChooser;
import GUI.DatabaseLogin;
import GUI.GeneralText;
import GUI.Player.PlayerInfoDialog;
import GUI.Player.PlayerText;
import GUI.Player.PlayerUI;
import Interface.EventListener;
import Interface.GeneralControl;
import Interface.GeneralDataAccess;
import data.DataSource;
import data.PlayerDataAccess;
import data.database.SqlDialect;
import data.file.FileType;
import exceptions.*;
import model.DatabaseInfo;
import model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;


public class PlayerControl implements GeneralControl {
    private static final Logger logger = LoggerFactory.getLogger(PlayerControl.class);
    private PlayerDataAccess playerDA;
    private final List<EventListener<SortedMap<?,?>>> listeners = new ArrayList<>();

    @Override
    public void run() {
        PlayerUI playerUI = new PlayerUI(this);
        logger.debug("Trying to build player frame");
        playerUI.run();
        logger.info("Finished building player frame");
        try {
            playerDA.initializeRegionServer();
        } catch (FileManageException e) {
            logger.error("Failed to load region server file. Cause: {}",e.getMessage());
            notifyListeners("region_server_null", null);
            System.exit(0);
        }
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

    public void createFile() {
        logger.debug("Creating new file: Saving possible data before changing datasource...");
        save();
        logger.debug("Creating file: Fetching data source...");
        new DataSourceChooser(DataSource.FILE, this::handleDataSourceForCreateFile);
    }

    private void handleDataSourceForCreateFile(DataSource dataSource, Object dataType){
        try {
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder((FileType) dataType));
            logger.info("Path built successfully");
            playerDA.createNewFile();
            logger.info("File created successfully");
            playerDA.setDataSource(dataSource);
            notifyListeners("dataSource_set",null);
            logger.info("New file Data source is set to {}", dataSource);
            playerDA.clearData();
            logger.info("Player data is cleared successfully");
            notifyListeners("data_changed", playerDA.getPlayerMap());
        } catch (OperationCancelledException e) {
            logger.info("Failed to create new file. Cause: Operation cancelled");
        } catch (FileManageException e) {
            logger.error("Failed to create new file. Cause: {}", e.getMessage());
            notifyListeners("file_create_error", null);
        }
    }

    public void importData() {
        try {
            logger.debug("Importing data: Saving possible data before changing datasource...");
            save();
            logger.info("Fetching data source and data type");
            new DataSourceChooser(null, this::handleDataSourceForImportData);
        } catch (OperationCancelledException e) {
            logger.info("Failed to import data. Cause: Operation cancelled");
            notifyListeners("operation_cancelled", null);
        } catch (DataTypeException e){
            System.out.println(e.getMessage());
            logger.error(e.getMessage());
        }
    }

    private void handleDataSourceForImportData(DataSource dataSource, Object dataType){
        playerDA.setDataSource(dataSource);
        logger.info("Import data source is set to {}", dataSource);
        notifyListeners("dataSource_set",null);
        switch(dataType){
            case FileType ignore -> importFile((FileType) dataType);
            case SqlDialect ignore -> importDB(dataSource, (SqlDialect) dataType);
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        }
    }

    public void importFile(FileType fileType) {
        playerDA.setFileType(fileType);
        logger.info("File Type is set to {}", fileType);
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
            logger.info("Failed to import data from FILE. Cause: Operation cancelled");
            notifyListeners("operation_cancelled", null);
        }
    }

    public void importDB(DataSource dataSource, SqlDialect sqlDialect)  {
        logger.info("Importing data from database: Connecting to database...");
        try {
            DatabaseInfo databaseInfo = playerDA.getDefaultDatabaseInfo(sqlDialect);
            databaseInfo.setDataSource(dataSource);
            new DatabaseLogin(databaseInfo, this::handleDatabaseLoginForImport);
        } catch (ConfigErrorException e) {
            logger.error(e.getMessage());
            notifyListeners("config_error", null);
        }
    }

    private void handleDatabaseLoginForImport(DatabaseInfo databaseInfo){
        try {
            if(playerDA.connectDB(databaseInfo)){
                logger.info("Database connected successfully");
                playerDA.setDatabaseInfo(databaseInfo);
                logger.info("Reading data from database...");
                playerDA.read();
                logger.info("Data read successfully from database, refreshing UI");
                notifyListeners("data_changed", playerDA.getPlayerMap());
                logger.info("Finished importing from database");
                playerDA.disconnectDB();
                logger.info("Disconnected from database to release resources");
            }else{
                throw new DatabaseException("Failed to connect to database");
            }
        }  catch (DatabaseException e) {
            logger.error("Failed to connect to database. Cause: {}", e.getMessage());
            notifyListeners("db_login_failed",null);
        }
    }

    public void add() {
        logger.info("Adding player...");
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getPlayerMap().keySet(), new Player(), this::processPlayerInfoForAdd);
    }

    private void processPlayerInfoForAdd(Player player){
        playerDA.add(player);
        logger.info("Finished adding player.");
        notifyListeners("data_changed", playerDA.getPlayerMap());
        notifyListeners("added_player", null);
    }

    public void modify(int selected_player_id){
        logger.info("Modifying player with ID: {}", selected_player_id);
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getPlayer(selected_player_id), this::processPlayerInfoForModify);
    }

    private void processPlayerInfoForModify(Player player){
        playerDA.modify(player);
        logger.info("Finished updating player with ID: {}", player.getID());
        notifyListeners("modified_player", null);
        notifyListeners("data_changed", playerDA.getPlayerMap());
    }

    public void delete(int selected_player_id) {
        logger.info("Deleting player with ID: {}", selected_player_id);
        playerDA.delete(selected_player_id);
        notifyListeners("data_changed", playerDA.getPlayerMap());
        logger.info("Finished deleting player with ID: {}", selected_player_id);
        notifyListeners("deleted_player", null);
    }

    public void export() {
        logger.info("Exporting data: Checking if data exists...");
        if(playerDA.isEmpty()){
            logger.info("Exporting data cancelled: No player data found");
            notifyListeners("player_map_null", null);
            return;
        }
        new DataSourceChooser(null, this::handleDataSourceForExport);
    }

    private void handleDataSourceForExport(DataSource dataSource, Object dataType){
        switch (dataSource){
            case FILE -> exportFile((FileType) dataType);
            case DATABASE, HIBERNATE -> exportDB(dataSource, (SqlDialect) dataType);
        }
        logger.info("Finished exporting data.");
    }

    private void exportFile(FileType fileType) {
        logger.info("Exporting data to file...");
        playerDA.setFileType(fileType);
        try {
            playerDA.export();
            notifyListeners("exported_file", null);
        } catch (OperationCancelledException e) {
            logger.info("Failed to export data to file. Cause: Operation cancelled");
            notifyListeners("operation_cancelled", null);
        } catch (Exception e) {
            logger.error("Failed to export data to file. Cause: {}", e.getMessage());
        }
    }

    private void exportDB(DataSource target_source, SqlDialect target_dialect) {
        logger.info("Exporting data to database...");
        try {
            DatabaseInfo databaseInfo = playerDA.getDefaultDatabaseInfo(target_dialect);
            databaseInfo.setDataSource(target_source);
            new DatabaseLogin(databaseInfo, this::handleDatabaseLoginForExport);
        } catch (ConfigErrorException e) {
            logger.error("Failed to read default database config. Cause: {}", e.getMessage());
        }
    }

    private void handleDatabaseLoginForExport(DatabaseInfo databaseInfo) {
        try {
            if(playerDA.connectDB(databaseInfo)){
                logger.info("Exporting data to target database...");
                playerDA.exportDB(databaseInfo.getDataSource());
                logger.info("Data exported successfully to target database");
                notifyListeners("exported_db", null);
                logger.info("Finished exporting to database");
            }else{
                throw new DatabaseException("Failed to connect to database");
            }
        }  catch (DatabaseException e) {
            logger.error("Failed to export data to database. Cause: {}", e.getMessage());
            notifyListeners("db_login_failed",null);
        }
    }

    public void save(){
        logger.info("Saving data: Checking if data exists...");
        /*if(playerDA.isEmpty()){
            logger.info("Saving data cancelled: No player data found");
            return;
        }*/
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
                    playerDA.save();
                    playerDA.disconnectDB();
                    break;
            }
            logger.debug("Data saved successfully");
        } catch (DatabaseException e) {
            logger.error(e.getMessage());
            notifyListeners("config_error", null);
        }
        notifyListeners("data_saved", null);
    }

    public void changeLanguage(){
        logger.info("Changing language...");
        String language;
        try {
            language = switch(GeneralText.getDialog().selectionDialog("language")){
                case 0 -> "en";
                case 1 -> "es";
                case 2 -> "cn";
                default -> throw new IllegalStateException("Unexpected language value.");
            };
        } catch (OperationCancelledException e) {
            logger.info("Language changing operation cancelled");
            notifyListeners("operation_cancelled", null);
            return;
        }
        GeneralText.getDialog().setLanguage(language);
        PlayerText.getDialog().setLanguage(language);
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
