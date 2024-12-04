package control;

import GUI.DataSourceChooser;
import GUI.DatabaseLogin;
import GUI.GeneralText;
import GUI.Player.PlayerInfoDialog;
import GUI.Player.PlayerText;
import GUI.Player.PlayerUI;
import Interface.EventListener;
import Interface.GeneralControl;
import data.GeneralDataAccess;
import data.DataSource;
import data.PlayerDataAccess;
import data.database.SqlDialect;
import data.file.FileType;
import data.http.DataType;
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
        try {
            playerDA.initializeRegionServer();
        } catch (FileManageException e) {
            logger.error("Failed to load region server file. Cause: {}",e.getMessage());
            notifyListeners("region_server_null", null);
            System.exit(0);
        }
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

    public void createFile() {
        logger.debug("Create file: Processing...");
        save();
        logger.info("Create file: Calling DataSourceChooser...");
        new DataSourceChooser(DataSource.FILE, this::handleDataSourceForCreateFile);
        logger.info("Create file: Process finished!");
    }

    private void handleDataSourceForCreateFile(DataSource dataSource, Object dataType){
        logger.info("Handling Data source for Create file: Processing...");
        try {
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder((FileType) dataType));
            playerDA.createNewFile();
            playerDA.setDataSource(dataSource);
            notifyListeners("dataSource_set",null);
            playerDA.clearData();
            notifyListeners("data_changed", playerDA.getPlayerMap());
        } catch (OperationCancelledException e) {
            logger.info("Handling Data source for Create file: Failed with cause: Operation cancelled");
        } catch (FileManageException e) {
            logger.error("Handling Data source for Create file: Failed with cause: {}", e.getMessage());
            notifyListeners("file_create_error", null);
        }
        logger.info("Handling Data source for Create file: Process finished!");
    }

    public void importData() {
        logger.info("Import data: Processing...");
        try {
            save();
            logger.info("Import data: Calling Data source chooser...");
            new DataSourceChooser(null, this::handleDataSourceForImportData);
        } catch (OperationCancelledException e) {
            logger.info("Import data: Failed with cause: Operation cancelled");
            notifyListeners("operation_cancelled", null);
        } catch (DataTypeException e){
            logger.error("Import data: Failed with cause: {}", e.getMessage());
        }
        logger.info("Import data: Process finished!");
    }

    private void handleDataSourceForImportData(DataSource dataSource, Object dataType){
        logger.info("Handling Data source for Import data: Processing...");
        playerDA.setDataSource(dataSource);
        notifyListeners("dataSource_set",null);
        switch(dataType){
            case FileType ignore -> importFile((FileType) dataType);
            case SqlDialect ignore -> importDB(dataSource, (SqlDialect) dataType);
            case DataType ignore -> importHttp((DataType) dataType);
            default -> throw new IllegalStateException("Unexpected value: " + dataType);
        }
        logger.info("Handling Data source for Import data: Process finished!");
    }

    private void importFile(FileType fileType) {
        logger.info("Import file: Processing...");
        playerDA.setFileType(fileType);
        logger.info("Import file: Fetching file path...");
        String file_path;
        try {
            file_path = GeneralDataAccess.getPath(playerDA.getFileType());
            playerDA.setFilePath(file_path);
            playerDA.read();
            notifyListeners("data_changed", playerDA.getPlayerMap());
        } catch (OperationCancelledException e) {
            logger.info("Import file: Failed with cause: Operation cancelled");
            notifyListeners("operation_cancelled", null);
        }
        logger.info("Import file: Process finished!");
    }

    private void importDB(DataSource dataSource, SqlDialect sqlDialect)  {
        logger.info("Import DB: Processing...");
        try {
            logger.info("Import DB: Fetching default database information...");
            DatabaseInfo databaseInfo = playerDA.getDefaultDatabaseInfo(sqlDialect);
            databaseInfo.setDataSource(dataSource);
            logger.info("Import DB: Calling DatabaseLogin...");
            new DatabaseLogin(databaseInfo, this::handleDatabaseLoginForImport);
        } catch (ConfigErrorException e) {
            logger.error("Import DB: Failed to read configuration with cause: {}", e.getMessage());
            notifyListeners("config_error", null);
        }
        logger.info("Import DB: Process finished!");
    }

    private void handleDatabaseLoginForImport(DatabaseInfo databaseInfo){
        logger.info("Handling DatabaseLogin for Import data: Processing...");
        try {
            if(playerDA.connectDB(databaseInfo)){
                playerDA.setDatabaseInfo(databaseInfo);
                playerDA.read();
                notifyListeners("data_changed", playerDA.getPlayerMap());
            }else{
                throw new DatabaseException("Failed to connect to database");
            }
        }  catch (DatabaseException e) {
            logger.error("Handling DatabaseLogin for Import data: Failed to read from database with cause: {}", e.getMessage());
            notifyListeners("db_login_failed",null);
            clearDataSource();
        }
        logger.info("Handling DatabaseLogin for Import data: Process finished!");
    }

    private void importHttp(DataType dataType) {
        logger.info("Import HTTP: Processing...");
        try{
            playerDA.setDataType(dataType);
            playerDA.read();
            notifyListeners("data_changed", playerDA.getPlayerMap());
        }catch (OperationCancelledException e) {
            logger.info("Import HTTP: Failed with cause: Operation cancelled");
        }
    }

    public void add() {
        logger.info("Add: Processing...");
        logger.info("Add: Calling Player info dialog...");
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getPlayerMap().keySet(), new Player(), this::handlePlayerInfoForAdd);
        logger.info("Add: Process finished!");
    }

    private void handlePlayerInfoForAdd(Player player){
        logger.info("Handle player info for add: Processing...");
        playerDA.add(player);
        notifyListeners("data_changed", playerDA.getPlayerMap());
        notifyListeners("added_player", null);
        logger.info("Handle player info for add: Process finished!");
    }

    public void modify(int selected_player_id){
        logger.info("Modify: Processing...");
        logger.info("Modify: Calling Player info dialog with player id: {}", selected_player_id);
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getPlayer(selected_player_id), this::handlePlayerInfoForModify);
        logger.info("Modify: Process finished!");
    }

    private void handlePlayerInfoForModify(Player player){
        logger.info("Handle player info for modify: Processing...");
        playerDA.modify(player);
        notifyListeners("modified_player", null);
        notifyListeners("data_changed", playerDA.getPlayerMap());
        logger.info("Handle player info for modify: Process finished!");
    }

    public void delete(int selected_player_id) {
        logger.info("Delete: Processing...");
        playerDA.delete(selected_player_id);
        notifyListeners("data_changed", playerDA.getPlayerMap());
        notifyListeners("deleted_player", null);
        logger.info("Delete: Process finished!");
    }

    public void export() {
        logger.info("Export: Processing...");
        logger.info("Export: Checking current player data...");
        if(playerDA.isEmpty()){
            logger.info("Export: Process cancelled with cause: No player data found");
            notifyListeners("player_map_null", null);
            return;
        }
        logger.info("Export: Calling DataSourceChooser...");
        new DataSourceChooser(null, this::handleDataSourceForExport);
        logger.info("Export: Process finished!");
    }

    private void handleDataSourceForExport(DataSource dataSource, Object dataType){
        logger.info("Handle DataSource for Export data: Processing...");
        switch (dataSource){
            case FILE -> exportFile((FileType) dataType);
            case DATABASE, HIBERNATE -> exportDB(dataSource, (SqlDialect) dataType);
        }
        logger.info("Handle DataSource for Export data: Process finished!");
    }

    private void exportFile(FileType fileType) {
        logger.info("Export File: Processing...");
        playerDA.setFileType(fileType);
        try {
            playerDA.exportFile();
            notifyListeners("exported_file", null);
        } catch (OperationCancelledException e) {
            logger.info("Failed to exportFile data to file. Cause: Operation cancelled");
            notifyListeners("operation_cancelled", null);
        } catch (Exception e) {
            logger.error("Failed to exportFile data to file. Cause: {}", e.getMessage());
            notifyListeners("export_failed", null);
        }
        logger.info("Export File: Process finished!");
    }

    private void exportDB(DataSource target_source, SqlDialect target_dialect) {
        logger.info("Export DB: Processing...");
        try {
            DatabaseInfo databaseInfo = playerDA.getDefaultDatabaseInfo(target_dialect);
            databaseInfo.setDataSource(target_source);
            logger.info("Export DB: Calling DataSourceChooser...");
            new DatabaseLogin(databaseInfo, this::handleDatabaseLoginForExport);
        } catch (ConfigErrorException e) {
            logger.error("Failed to read default database config. Cause: {}", e.getMessage());
            notifyListeners("config_error", null);
        }
        logger.info("Export DB: Process finished!");
    }

    private void handleDatabaseLoginForExport(DatabaseInfo databaseInfo) {
        logger.info("Handle DatabaseLogin for Export data: Processing...");
        try {
            if(playerDA.connectDB(databaseInfo)){
                playerDA.exportDB(databaseInfo.getDataSource());
                notifyListeners("exported_db", null);
            }else{
                throw new DatabaseException("Failed to connect to database");
            }
        }  catch (DatabaseException e) {
            logger.error("Failed to exportFile data to database. Cause: {}", e.getMessage());
            notifyListeners("db_login_failed",null);
        }
        logger.info("Handle DatabaseLogin for Export data: Process finished!");
    }

    public void save(){
        logger.info("Save: Processing...");
        DataSource dataSource = playerDA.getDataSource();
        try {
            switch (dataSource){
                case NONE:
                    logger.info("Save: Current data source is NONE, returning...");
                    return;
                case FILE:
                    logger.info("Save: Current data source is FILE, saving...");
                    playerDA.save();
                    break;
                case DATABASE, HIBERNATE:
                    logger.info("Save: Current data source is DATABASE or HIBERNATE, saving...");
                    playerDA.save();
                    break;
                case PHP:
                    logger.info("Save: Current data source is PHP, saving...");
                    playerDA.save();
                    break;
            }
        } catch (DatabaseException e) {
            logger.error("Save: Failed to save data via database with cause: {}", e.getMessage());
            notifyListeners("config_error", null);
        }
        notifyListeners("data_saved", null);
        logger.info("Save: Process finished!");
    }

    private void clearDataSource(){
        logger.info("Clear DataSource: Processing...");
        playerDA.setDataSource(DataSource.NONE);
        playerDA.setFileType(FileType.NONE);
        playerDA.setDatabaseInfo(new DatabaseInfo());
        logger.info("Clear DataSource: Process finished!");
    }

    public void changeLanguage(){
        logger.info("Change language: Processing...");
        String language;
        try {
            language = switch(GeneralText.getDialog().selectionDialog("language")){
                case 0 -> "en";
                case 1 -> "es";
                case 2 -> "cn";
                default -> throw new IllegalStateException("Unexpected language value.");
            };
            logger.info("Change language: Language is set to {}", language);
        } catch (OperationCancelledException e) {
            logger.info("Change language: Failed to change language with cause: Operation cancelled");
            notifyListeners("operation_cancelled", null);
            return;
        }
        GeneralText.getDialog().setLanguage(language);
        PlayerText.getDialog().setLanguage(language);
        notifyListeners("language_changed",null);
        logger.info("Change language: Process finished!");
    }

    public void addListener(EventListener<SortedMap<?,?>> listener){
        listeners.add(listener);
    }

    private void notifyListeners(String event, SortedMap<?,?> data){
        logger.info("Notify listener: Processing with code: {}", event);
        for(EventListener<SortedMap<?,?>> listener : listeners){
            listener.onEvent(event, data);
        }
        logger.info("Notify listener: Process finished!");
    }

}
