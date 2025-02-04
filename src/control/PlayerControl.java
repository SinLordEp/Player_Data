package control;

import GUI.DataSourceChooser;
import GUI.DatabaseLogin;
import GUI.LogStage;
import GUI.Player.PlayerInfoDialog;
import GUI.Player.PlayerText;
import GUI.Player.PlayerUI;
import Interface.EventListener;
import Interface.GeneralControl;
import data.DataSource;
import data.GeneralDataAccess;
import data.PlayerDataAccess;
import data.database.SqlDialect;
import data.file.FileType;
import data.http.PhpType;
import exceptions.*;
import model.DatabaseInfo;
import model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * The PlayerControl class is responsible for managing the core functionalities of the Player UI,
 * including data access, event handling, and operations related to file, database, and UI control.
 * The class interacts with data sources, manages the lifecycle of the Player UI, and ensures proper
 * handling of events and exceptions during operations such as importing, exporting, creating, and
 * modifying player data.
 * <p>
 * This class implements the GeneralControl interface, providing implementations for
 * the required methods to initialize, manage, and close the Player UI.
 * @author SIN
 */
public class PlayerControl implements GeneralControl {
    private static final Logger logger = LoggerFactory.getLogger(PlayerControl.class);
    private PlayerDataAccess playerDA;
    private final List<EventListener<TreeMap<Integer, Player>>> listeners = new ArrayList<>();

    /**
     * Executes the logic for initializing and managing the player user interface (UI)
     * and its associated dependencies.
     * <p>
     * This method initializes the {@code PlayerUI} object, attempts to set up the region
     * server by calling the {@code initializeRegionServer} method on the {@code playerDA}
     * object, and handles any exceptions that may occur during this process. It logs errors
     * if the region server initialization fails and terminates the application in such cases.
     * If the initialization succeeds, it proceeds to build the player UI by invoking the
     * {@code run} method of the {@code PlayerUI} instance while logging relevant debugging
     * and informational messages.
     * <p>
     * In case of an error during region server setup, the method notifies listeners of a
     * failure event with the identifier "region_server_null".
     */
    @Override
    public void run() {
        PlayerUI playerUI = new PlayerUI(this);
        try {
            playerDA.initializeRegionServer();
        } catch (FileManageException e) {
            logger.error("Failed to load region server file. Cause: {}", e.getMessage());
            notifyEvent("region_server_null", null);
            System.exit(0);
        }
        playerUI.run();
    }

    /**
     * Set the data access for current controller
     * @param DA data access manage for player
     */
    @Override
    public void setDA(GeneralDataAccess DA) {
        this.playerDA = (PlayerDataAccess) DA;
    }

    /**
     * Callback method triggered when the application window is in the process of closing.
     * This method is invoked to handle clean-up tasks and terminate the application gracefully.
     * <p>
     * Implementation details:
     * - Logs a debug message indicating the initiation of the window closing procedure.
     * - Invokes the `save` method to persist any pending data or state before termination.
     * - Logs an informational message indicating the application shutdown process.
     * - Calls `System.exit(0)` to terminate the application with an exit status of 0.
     */
    @Override
    public void onWindowClosing() {
        logger.debug("Trigger Player window closing procedure...");
        save();
        logger.info("Shutting down");
        System.exit(0);
    }

    /**
     * Handles the creation of a file.
     * <p>
     * This method processes the file creation request by performing the following steps:
     * <p>
     * 1. Logs debug and info messages to track the progress of file creation.<br>
     * 2. Calls the {@code save()} method to persist any relevant data.<br>
     * 3. Initializes a {@code DataSourceChooser} with the {@code DataSource.FILE} type.
     * <p>
     * The method also provides a callback, {@code handleDataSourceForCreateFile}, to handle
     * specific operations related to the chosen data source.
     */
    public void createFile() {
        save();
        logger.info("Calling DataSourceChooser...");
        new DataSourceChooser(DataSource.FILE, this::handleDataSourceForCreateFile);
        logger.info("File created");
    }

    /**
     * Handles the processing of a data source for creating a new file.
     * This method is responsible for building the file path, creating a new file,
     * associating the specified data source with the file, and notifying listeners
     * of data changes or errors.
     * <p>
     * The method performs the following steps:
     * <p>
     * - Builds a new file path using the specified {@code dataType}.
     * <p>
     * - Creates a new file at the specified path.
     * <p>
     * - Sets the provided data source for the file.
     * <p>
     * - Notifies listeners of operations, including creating the file
     *   and data source updates. If an operation fails, it logs the error
     *   and notifies listeners about the failure.
     * <p>
     * - Clears the internal data after processing.
     *
     * @param dataSource The data source that needs to be handled and associated with the newly created file.
     * @param dataType   The type of the file to be created. This is expected to be of type {@code FileType}.
     */
    private void handleDataSourceForCreateFile(DataSource dataSource, Object dataType){
        logger.info("Processing DataSource-{} and DataType-{} to create file", dataSource, dataType);
        try {
            playerDA.setFilePath(GeneralDataAccess.newPathBuilder((FileType) dataType));
            playerDA.createNewFile();
            playerDA.setDataSource(dataSource);
            notifyEvent("dataSource_set",null);
            playerDA.clearData();
            notifyEvent("data_changed", playerDA.getPlayerMap());
        } catch (OperationCancelledException e) {
            logger.info("File creation cancelled");
        } catch (FileManageException e) {
            logger.error("Failed to create file. Cause: {}", e.getMessage());
            notifyEvent("file_create_error", null);
        }
    }

    /**
     * Initiates the process to import data into the system.
     *
     * <p>The method performs the following sequential operations:
     * <p>
     * 1. Logs the beginning of the import operation.
     * <p>
     * 2. Calls the `save` method to persist any current changes before proceeding.
     * <p>
     * 3. Opens a `DataSourceChooser` dialog to allow the user to select the source of the data to be imported.
     *    - The selected data source is processed through the `handleDataSourceForImportData` callback method.
     *
     * <p>If the user cancels the operation or an error occurs, the method manages the exceptions as follows:
     * <p>
     * - Catches `OperationCancelledException` and logs the cancellation. Additionally, it notifies registered
     *   event listeners with the `"operation_cancelled"` event.
     * - Catches `DataTypeException` and logs the error message of the exception.
     *
     * <p>This method is designed to manage the selection and configuration of the data source for importing data,
     * ensuring that any necessary pre-import actions (e.g., saving current state) are performed and that relevant
     * errors or cancellations are gracefully handled.
     */
    public void importData() {
        logger.info("Importing data...");
        try {
            save();
            logger.info("Calling Data source chooser...");
            new DataSourceChooser(null, this::handleDataSourceForImportData);
        } catch (OperationCancelledException e) {
            logger.info("Data source chooser cancelled");
            notifyEvent("operation_cancelled", null);
        } catch (DataTypeException e){
            logger.error("Failed to import data. Cause: {}", e.getMessage());
        }
    }

    /**
     * Handles the data source and data type for importing data into the system.
     * <p>
     * This callback method performs the following operations:
     * <p>
     * 1. Configures the specified {@code DataSource} in the current system.
     * <p>
     * 2. Based on the {@code dataType} provided, determines the type of import process to execute:
     *    - Invokes {@code importFile(FileType)} if the {@code dataType} is a {@code FileType}.
     *    - Invokes {@code importDB(DataSource, SqlDialect)} if the {@code dataType} is a {@code SqlDialect}.
     *    - Invokes {@code importPHP(DataType)} if the {@code dataType} is a {@code DataType}.
     * <p>
     * 3. Notifies listeners about the setting of the data source.
     * <p>
     * 4. Handles any unexpected {@code dataType} by throwing an {@code IllegalStateException}.
     * <p>
     * This method encapsulates the logic for handling different types of imports dynamically
     * based on the data source and the associated data type context.
     *
     * @param dataSource The selected data source from which the data will be imported.
     *                   This can be one of {@code NONE}, {@code FILE}, {@code DATABASE},
     *                   {@code HIBERNATE}, or {@code PHP}.
     * @param dataType   The type of data to be processed during the import operation.
     *                   This defines the specific logic for handling the provided data.
     */
    private void handleDataSourceForImportData(DataSource dataSource, Object dataType){
        logger.info("Processing DataSource-{} and DataType-{} to import data", dataSource, dataType);
        playerDA.setDataSource(dataSource);
        notifyEvent("dataSource_set",null);
        switch(dataSource){
            case FILE -> importFile((FileType) dataType);
            case DATABASE, HIBERNATE -> importDB(dataSource, (SqlDialect) dataType);
            case PHP -> importPHP((PhpType) dataType);
            case OBJECTDB, BASEX, MONGO -> importDB(dataSource, SqlDialect.NONE);
            default -> throw new IllegalStateException("Unexpected Data Source: " + dataType);
        }
    }

    /**
     * Imports a file into the system based on the specified file type.
     * <p>
     * This method performs the following operations:
     * 1. Logs the start of the file import process.
     * 2. Sets the provided file type in the data access object.
     * 3. Retrieves the file path associated with the specified file type and sets it in the data access object.
     * 4. Reads the file data and updates the internal data map.
     * 5. Notifies listeners about data changes in case of a successful import, or about the operation cancellation if interrupted.
     * 6. Logs the completion of the file import process.
     * <p>
     * The method handles exceptions such as `OperationCancelledException` and ensures listeners are properly notified in such cases.
     *
     * @param fileType The type of the file to be imported. This defines the format and properties
     *                 of the file, such as {@code TXT}, {@code DAT}, or {@code XML}.
     */
    private void importFile(FileType fileType) {
        logger.info("Processing file type-{} to import", fileType);
        playerDA.setFileType(fileType);
        logger.info("Fetching file path");
        String file_path;
        try {
            file_path = GeneralDataAccess.getPath(playerDA.getFileType());
            playerDA.setFilePath(file_path);
            playerDA.read();
            notifyEvent("data_changed", playerDA.getPlayerMap());
        } catch (OperationCancelledException e) {
            logger.info("Import from file cancelled");
            notifyEvent("operation_cancelled", null);
        }
        logger.info("File data imported");
    }

    /**
     * Initiates the process to import data from a database into the system.
     * <p>
     * This method performs the following steps:
     * 1. Logs the start of the database import process.
     * 2. Retrieves default database information for the specified SQL dialect.
     * 3. Configures the database information with the provided data source.
     * 4. Establishes a database connection by invoking the {@code DatabaseLogin} class
     *    and handles the logic through the {@code handleDatabaseLoginForImport} callback method.
     * 5. Catches configuration-related exceptions and logs the error, notifying listeners
     *    of the failure event.
     * 6. Logs the completion of the database import process.
     *
     * @param dataSource The source of the data to be imported (e.g., DATABASE, FILE, PHP, etc.).
     *                   This defines the entry point for the data being handled.
     * @param sqlDialect The SQL dialect being used for the database import, such as MYSQL or SQLITE.
     *                   This specifies the type of database operation to configure.
     */
    private void importDB(DataSource dataSource, SqlDialect sqlDialect)  {
        logger.info("Processing DataSource-{} and SQL Dialect-{} to import", dataSource, sqlDialect);
        try {
            logger.info("Fetching default database information");
            DatabaseInfo databaseInfo = playerDA.getDefaultDatabaseInfo(sqlDialect, dataSource);
            databaseInfo.setDataSource(dataSource);
            logger.info("Calling DatabaseLogin");
            new DatabaseLogin(databaseInfo, this::handleDatabaseLoginForImport);
        } catch (ConfigErrorException e) {
            logger.error("Import DB: Failed to read configuration with cause: {}", e.getMessage());
            notifyEvent("config_error", null);
        }
        logger.info("Import DB: Process finished!");
    }

    /**
     * Handles the process of logging into the database and importing data.
     * This method attempts to establish a connection to the database using the information
     * provided in the {@code databaseInfo} object. If the connection is successful, it sets
     * the database information, reads data from the database, and notifies listeners of the
     * change. If the connection or reading process fails, it logs the error, notifies listeners
     * of the failure, and clears any data source.
     *
     * <p>This is typically a callback method used to manage the database login and import data
     * workflow for a specific operation.
     *
     * @param databaseInfo An object containing the necessary information to establish a
     *                     connection to the database (e.g., credentials, database URL, etc.).
     */
    private void handleDatabaseLoginForImport(DatabaseInfo databaseInfo){
        logger.info("Handling DatabaseLogin for Import data: Processing...");
        try {
            playerDA.setDatabaseInfo(databaseInfo);
            playerDA.read();
            notifyEvent("data_changed", playerDA.getPlayerMap());
        }  catch (DatabaseException e) {
            logger.error("Handling DatabaseLogin for Import data: Failed to read from database with cause: {}", e.getMessage());
            notifyEvent("db_login_failed",null);
            clearDataSource();
        }
        logger.info("Handling DatabaseLogin for Import data: Process finished!");
    }

    /**
     * Handles the import process for PHP data. This method processes the given data type
     * by setting it, reading required information, and notifying listeners about changes
     * or errors accordingly. It logs the process and handles exceptions that might
     * occur during the PHP data import operation.
     *
     * @param phpType The data type to be set and processed during the PHP import operation.
     *                 This parameter is applied to fetch and manage data through the
     *                 provided data access layer.
     */
    private void importPHP(PhpType phpType) {
        logger.info("Processing PHP type-{} to import", phpType);
        try{
            playerDA.setPhpType(phpType);
            playerDA.read();
            notifyEvent("data_changed", playerDA.getPlayerMap());
        }catch (OperationCancelledException e) {
            logger.info("Importe from PHP cancelled");
            notifyEvent("operation_cancelled", null);
        }catch (HttpPhpException e){
            logger.error("Failed to import from php server. Cause: {}", e.getMessage());
            notifyEvent("php_error",null);
        }
    }


    /**
     * Initiates the process for adding a new player entry. This method performs the following:
     * <p>
     * - Logs the start of the add process.
     * - Opens a Player Info Dialog, allowing the user to input or edit player information.
     * - Passes data related to region-server mapping and existing player keys to the dialog.
     * - Utilizes a callback method, {@code handlePlayerInfoForAdd}, to process input data from the dialog.
     * - Logs the completion of the add process.
     * <p>
     * The method relies on {@code PlayerInfoDialog} for handling user interactions and uses
     * {@code logger} to record the workflow.
     */
    public void add() {
        logger.info("Calling Player info dialog...");
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getPlayerMap().keySet(), new Player(), this::handlePlayerInfoForAdd);
    }

    /**
     * Handles the addition of a player's information by processing the given player object.
     * It performs actions such as adding the player to the player data access system,
     * notifying registered listeners about the changes, and logging the process events.
     *
     * @param player The Player object containing the details to be added.
     */
    private void handlePlayerInfoForAdd(Player player){
        playerDA.add(player);
        notifyEvent("data_changed", playerDA.getPlayerMap());
        notifyEvent("added_player", null);
        logger.info("Player with id-{} has been added", player.getID());
    }

    /**
     * Modifies the details of a selected player by launching the PlayerInfoDialog
     * and processing the player's information.
     * <p>
     * This method initiates a sequence to obtain and possibly alter player details.
     * The callback method {@code handlePlayerInfoForModify} is triggered to handle
     * the information provided in the dialog.
     *
     * @param selected_player_id the unique identifier of the player to be modified
     */
    public void modify(int selected_player_id){
        logger.info("Calling Player info dialog with player id: {}", selected_player_id);
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getPlayer(selected_player_id), this::handlePlayerInfoForModify);
    }

    /**
     * Handles the modification of player information by updating the player data,
     * and notifying the relevant listeners about the changes.
     *
     * @param player The player object containing the information to be modified.
     */
    private void handlePlayerInfoForModify(Player player){
        playerDA.modify(player);
        notifyEvent("modified_player", null);
        notifyEvent("data_changed", playerDA.getPlayerMap());
        logger.info("Player with id-{} has been modified", player.getID());
    }

    /**
     * Deletes a player based on the provided player ID.
     * This method removes a player from the data source by invoking the delete operation
     * on the data access object. It also notifies any registered listeners about the changes.
     *
     * @param selected_player_id the unique identifier of the player to be deleted
     */
    public void delete(int selected_player_id) {
        playerDA.delete(selected_player_id);
        notifyEvent("data_changed", playerDA.getPlayerMap());
        notifyEvent("deleted_player", null);
        logger.info("Player with id-{} has been deleted", selected_player_id);
    }

    /**
     * Initiates the export process for player data. This method performs a series of checks and operations
     * to ensure that the necessary data is available and to prompt the user for a data export location.
     * <p>
     * The method logs the progress of the export process step-by-step and performs the following actions:
     * 1. Validates if player data exists. If no data is found, the process is canceled, appropriate logs
     *    are written, and listeners are notified with an error cause ("player_map_null").
     * 2. If player data is available, it initializes a DataSourceChooser to allow the user to select a data
     *    source for export, passing a callback method `handleDataSourceForExport` to handle the subsequent
     *    actions once the data source is chosen.
     * <p>
     * Callback Method:
     * The `handleDataSourceForExport` method is invoked asynchronously via the DataSourceChooser instance,
     * and is responsible for processing the export operation once the user selects a destination for data export.
     */
    public void export() {
        logger.info("Checking current player data");
        if(playerDA.isEmpty()){
            logger.info("Exportation is cancelled. Cause: No player data found");
            notifyEvent("player_map_null", null);
            return;
        }
        new DataSourceChooser(null, this::handleDataSourceForExport);
    }

    /**
     * Handles the specified data source for exporting data. This method processes the given
     * data source type and delegates the export operation to the appropriate handler based
     * on the provided data type.
     *
     * @param dataSource the type of data source to be processed for export. Supported values
     *                   include FILE, DATABASE, HIBERNATE, and PHP.
     * @param dataType   the specific data type associated with the data source. The type of
     *                   this parameter varies depending on the data source. For example:
     *                   <p>
     *                   - For FILE, dataType is an instance of FileType.<br>
     *                   - For DATABASE and HIBERNATE, dataType is an instance of SqlDialect.<br>
     *                   - For PHP, dataType is an instance of DataType.
     */
    private void handleDataSourceForExport(DataSource dataSource, Object dataType){
        logger.info("Processing DataSource-{} and DataType-{} to export", dataSource, dataType);
        switch (dataSource){
            case FILE -> exportFile((FileType) dataType);
            case DATABASE, HIBERNATE -> exportDB(dataSource, (SqlDialect) dataType);
            case PHP -> exportPHP((PhpType) dataType);
            case OBJECTDB, BASEX, MONGO -> exportDB(dataSource, SqlDialect.NONE);
            default -> throw new IllegalArgumentException("Unknown data source: " + dataSource);
        }
    }

    /**
     * Exports data to a file based on the specified file type.
     * The method sets the file type for the player data access object and attempts to perform
     * the export operation. Any relevant listeners are notified based on the outcome of the export.
     *
     * @param fileType The type of file to which the data will be exported.
     *                 This determines the format and structure of the exported file.
     *                 Must not be null, and should represent a valid file type.
     */
    private void exportFile(FileType fileType) {
        logger.info("Processing file type-{} to export", fileType);
        playerDA.setFileType(fileType);
        try {
            playerDA.exportFile();
            notifyEvent("exported_file", null);
        } catch (OperationCancelledException e) {
            logger.info("Failed to exportFile data to file. Cause: Operation cancelled");
            notifyEvent("operation_cancelled", null);
        } catch (Exception e) {
            logger.error("Failed to exportFile data to file. Cause: {}", e.getMessage());
            notifyEvent("export_failed", null);
        }
        logger.info("Completed exporting to file");
    }

    /**
     * Exports the database to the specified data source using the provided SQL dialect.
     * This method manages the configuration, prepares the database connection,
     * and invokes a callback to handle the login for exporting the database.
     *
     * @param target_source The target data source where the database will be exported.
     * @param target_dialect The SQL dialect to be used for the export operation.
     */
    private void exportDB(DataSource target_source, SqlDialect target_dialect) {
        logger.info("Processing target DataSource-{} and SQL dialect {} to export", target_source,  target_dialect);
        try {
            DatabaseInfo databaseInfo = playerDA.getDefaultDatabaseInfo(target_dialect, target_source);
            databaseInfo.setDataSource(target_source);
            new DatabaseLogin(databaseInfo, this::handleDatabaseLoginForExport);
        } catch (ConfigErrorException e) {
            logger.error("Failed to read default database config. Cause: {}", e.getMessage());
            notifyEvent("config_error", null);
        }
    }

    /**
     * Handles the database login specifically for exporting data. This method attempts to
     * establish a connection to the database using the provided {@code DatabaseInfo} object.
     * If the connection is successful, it proceeds to export the database data. In case of
     * failure, appropriate notifications are triggered.
     * <p>
     * This method acts as a callback handler and may notify external listeners about the
     * outcome of the login and export process.
     *
     * @param databaseInfo the {@code DatabaseInfo} object containing the necessary information
     *                     such as database configuration, credentials, and data source details
     *                     required to connect and perform the export operation.
     */
    private void handleDatabaseLoginForExport(DatabaseInfo databaseInfo) {
        try {
            playerDA.exportDB(databaseInfo);
            notifyEvent("exported_db", null);
        }  catch (DatabaseException e) {
            logger.error("Failed to exportFile data to database. Cause: {}", e.getMessage());
            notifyEvent("db_login_failed",null);
        }
    }

    /**
     * Exports the provided data type using PHP and notifies listeners of the result.
     * <p>
     * This method processes the given data type by delegating the export operation
     * to the {@code playerDA.exportPHP} method. It logs the export operation, and
     * upon completion or failure, it notifies any registered listeners with the
     * appropriate status. If an exception occurs during the export process, it
     * logs the error and triggers a "php_error" notification.
     *
     * @param phpType the data to be exported. The specific implementation of
     *                 the export is determined by the `playerDA.exportPHP` method.
     */
    private void exportPHP(PhpType phpType) {
        logger.info("Processing PHP type-{} to export", phpType);
        try{
            playerDA.exportPHP(phpType);
            notifyEvent("exported_php", null);
        }catch (HttpPhpException e){
            logger.error("Export PHP: Failed with cause: {}", e.getMessage());
            notifyEvent("php_error", null);
        }
    }

    /**
     * Saves the data based on the current data source type.
     * <p>
     * The method determines the data source configured for the playerDA object and executes the saving logic accordingly.
     * It logs specific messages to indicate the progress throughout the execution process.
     *
     * <p>Supported data sources:
     * - NONE: Does nothing and returns without saving.
     * - FILE: Invokes playerDA to save data to a file.
     * - DATABASE or HIBERNATE: Invokes playerDA to save data to a database.
     * - PHP: Invokes playerDA to save data through a PHP interface.
     * <p>
     * In case of a DatabaseException during database or Hibernate save operations,
     * an error message is logged, and listeners are notified with the "config_error" event key.
     * Regardless of data source type, listeners are always notified with the "data_saved" event key upon successful execution.
     * <p>
     * This function ensures proper logging and listener notification during its operation.
     */
    public void save(){
        if(!playerDA.isDataChanged()){
            logger.info("Save: Process cancelled with cause: No datas were changed");
            return;
        }
        try {
            if (Objects.requireNonNull(playerDA.getDataSource()) == DataSource.NONE) {
                logger.info("Current data source is NONE, returning...");
                return;
            } else {
                logger.info("Current data source is {}, saving...", playerDA.getDataSource());
                playerDA.save();
            }
        } catch (DatabaseException e) {
            logger.error("Failed to save data via database. Cause: {}", e.getMessage());
            notifyEvent("config_error", null);
        }
        notifyEvent("data_saved", null);
        logger.info("Data saved");
    }

    /**
     * Clears the current data source of the player data access object (playerDA).
     * <p>
     * This method resets the data source by setting it to {@code DataSource.NONE},
     * clears the file type by setting it to {@code FileType.NONE}, and initializes
     * the database information to a new instance of {@code DatabaseInfo}.
     * Logging statements are included to indicate the start and completion of this
     * process.
     */
    private void clearDataSource(){
        logger.info("Clearing current data source");
        playerDA.setDataSource(DataSource.NONE);
        playerDA.setFileType(FileType.NONE);
        playerDA.setDatabaseInfo(new DatabaseInfo());
        logger.info("Completed clearing current data source");
    }

    /**
     * Changes the application's language based on user selection.
     * <p>
     * Prompts a dialog for the user to select a language. The available language options are:
     * - English ("en")
     * - Spanish ("es")
     * - Chinese ("cn")
     * <p>
     * Once a language is selected, it updates and applies the language choice in the relevant components.
     * Notifies listeners about the language change event or operation cancellation.
     * <p>
     * In case of operation cancellation, an appropriate notification is sent to the listeners
     * with the "operation_cancelled" event.
     * <p>
     * If the user makes an invalid selection, the method throws an exception.
     * Logging is implemented to track the process at different stages.
     */
    public void changeLanguage(){
        String language;
        try {
            language = switch(PlayerText.getDialog().selectionDialog("language")){
                case 0 -> "en";
                case 1 -> "es";
                case 2 -> "cn";
                default -> throw new IllegalStateException("Unexpected language value.");
            };
            logger.info("Change language: Language is set to {}", language);
        } catch (OperationCancelledException e) {
            logger.info("Change language: Failed to change language with cause: Operation cancelled");
            notifyEvent("operation_cancelled", null);
            return;
        }
        PlayerText.getDialog().setLanguage(language);
        notifyEvent("language_changed",null);
    }

    /**
     * Adds an event listener to the list of listeners that will be notified of relevant events.
     * The listener will typically define the actions to be performed when an event occurs.
     *
     * @param listener the event listener to be added. It must implement the EventListener interface
     *                 and handle events related to a SortedMap with any key-value pair types.
     */
    public void addListener(EventListener<TreeMap<Integer, Player>> listener){
        listeners.add(listener);
    }

    /**
     * Notifies all registered listeners of a specific event and provides them with associated data.
     * Iterates through the list of EventListener instances and invokes their {@code onEvent} method.
     *
     * @param event the name or identifier of the event to notify about
     * @param data the data associated with the event, passed as a {@code SortedMap} object
     */
    private void notifyEvent(String event, TreeMap<Integer, Player> data){
        logger.info("Notifying listener with code: {}", event);
        for(EventListener<TreeMap<Integer, Player>> listener : listeners){
            listener.onEvent(event, data);
        }
    }

    private void notifyLog(LogStage stage, String... message){
        for(EventListener<TreeMap<Integer, Player>> listener : listeners){
            listener.onLog(stage, message);
        }
    }

}
