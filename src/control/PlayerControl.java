package control;

import GUI.DataSourceChooser;
import GUI.DatabaseLogin;
import GUI.TextHandler;
import GUI.LogStage;
import GUI.Player.PlayerInfoDialog;
import GUI.Player.PlayerUI;
import Interface.EventListener;
import Interface.GeneralControl;
import Interface.VerifiedEntity;
import data.*;
import data.file.FileType;
import data.http.PhpType;
import exceptions.OperationCancelledException;
import exceptions.PlayerExceptionHandler;
import model.DataInfo;
import model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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
    private PlayerDAO playerDA;
    private final List<EventListener<TreeMap<Integer, VerifiedEntity>>> listeners = new ArrayList<>();

    @Override
    public GeneralControl initialize() {
        playerDA = new PlayerDAO(PlayerParser.getInstance());
        return this;
    }

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
        addListener(playerUI);
        PlayerExceptionHandler.getInstance().addListener(playerUI);
        playerUI.run();
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
        saveToFile();
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
        saveToFile();
        new DataSourceChooser(new DataInfo(DataSource.FILE), this::handleDataSourceForCreateFile);
    }

    private void handleDataSourceForCreateFile(DataInfo dataInfo) {
        PlayerExceptionHandler
                .getInstance()
                .handle(() -> {
                    dataInfo.setUrl(GeneralDAO.newPathBuilder((FileType) dataInfo.getDataType()));
                    playerDA.createNewFile(dataInfo.getUrl());
                    playerDA.setDataInfo(dataInfo);
                    notifyEvent("dataSource_set",null);
                    playerDA.clearData();
                    notifyEvent("data_changed", playerDA.getDataContainer());
                }, "PlayerControl-handleDataSourceForCreateFile()", "createFile");
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
        saveToFile();
        new DataSourceChooser(new DataInfo(), this::handleDataSourceForImportData);
    }

    private void handleDataSourceForImportData(DataInfo dataInfo){
        playerDA.setDataInfo(dataInfo);
        notifyEvent("dataSource_set",null);
        switch(dataInfo.getDataType()){
            case FileType ignore :
                String file_path = PlayerExceptionHandler.getInstance()
                        .handle(() -> GeneralDAO.getPath((FileType) dataInfo.getDataType()), "GeneralDataAccess-getPath()", "getPath");
                dataInfo.setUrl(file_path);
                importData(dataInfo);
                break;
            case DataSource.DATABASE, DataSource.HIBERNATE, DataSource.OBJECTDB, DataSource.BASEX, DataSource.MONGO :
                PlayerExceptionHandler.getInstance().handle(() -> new DatabaseLogin(playerDA.getDefaultDatabaseInfo(dataInfo), this::importData),
                        "PlayerControl-importDB()", "default_database");
                break;
            case PhpType ignore : importData(dataInfo);
                break;
            default : throw new IllegalStateException("Unexpected Data Source: " + dataInfo.getDataType());
        }
    }

    private void importData(DataInfo dataInfo) {
        PlayerExceptionHandler.getInstance()
                .handle(() -> {
                    playerDA.findAll();
                    notifyEvent("data_changed", playerDA.getDataContainer());
                }, "PlayerControl-importData()" , "import", "\n>>>" + dataInfo.getUrl());
    }

    public void search() {
        new DataSourceChooser(new DataInfo(DataSource.NONE), this::handleDataSourceForSearch);
    }

    private void handleDataSourceForSearch(DataInfo dataInfo){
        notifyEvent("dataSource_set",null);
        switch(dataInfo.getDataType()){
            case DataSource.DATABASE, DataSource.HIBERNATE, DataSource.OBJECTDB, DataSource.BASEX, DataSource.MONGO :
                PlayerExceptionHandler.getInstance().handle(() -> new DatabaseLogin(playerDA.getDefaultDatabaseInfo(dataInfo), this::searchID),
                        "PlayerControl-searchDB()", "default_database");
                break;
            case PhpType ignore : searchID(dataInfo); break;
            default : throw new IllegalStateException("Unexpected Data Source: " + dataInfo.getDataType());
        }
    }

    private void searchID(DataInfo dataInfo) {
        playerDA.setDataInfo(dataInfo);
        PlayerExceptionHandler.getInstance()
                .handle(() -> {
                    playerDA.findById();
                    notifyEvent("data_changed", playerDA.getDataContainer());
                }, "PlayerControl-searchID()", "search", "\n>>>" + dataInfo.getUrl());
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
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getDataContainer().keySet(), new Player(), this::handlePlayerInfoForAdd);
    }

    /**
     * Handles the addition of a player's information by processing the given player object.
     * It performs actions such as adding the player to the player data access system,
     * notifying registered listeners about the changes, and logging the process events.
     *
     * @param player The Player object containing the details to be added.
     */
    private void handlePlayerInfoForAdd(Player player){
        PlayerExceptionHandler.getInstance()
                .handle(() -> {
                            playerDA.update(DataOperation.ADD, player);
                            notifyEvent("data_changed", playerDA.getDataContainer());
                        },
                        "PlayerControl-handlePlayerInfoForAdd()", "addPlayer", ">>>ID: " + player.getID());
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
        new PlayerInfoDialog(playerDA.getRegion_server_map(), playerDA.getPlayerCopy(selected_player_id), this::handlePlayerInfoForModify);
    }

    /**
     * Handles the modification of player information by updating the player data,
     * and notifying the relevant listeners about the changes.
     *
     * @param player The player object containing the information to be modified.
     */
    private void handlePlayerInfoForModify(Player player){
        PlayerExceptionHandler.getInstance()
                .handle(() -> {
                    playerDA.update(DataOperation.MODIFY, player);
                    notifyEvent("data_changed", playerDA.getDataContainer());
                },"PlayerControl-handlePlayerInfoForModify()", "modifyPlayer", ">>>ID: " + player.getID());
    }

    /**
     * Deletes a player based on the provided player ID.
     * This method removes a player from the data source by invoking the delete operation
     * on the data access object. It also notifies any registered listeners about the changes.
     *
     * @param selected_player_id the unique identifier of the player to be deleted
     */
    public void delete(int selected_player_id) {
        PlayerExceptionHandler.getInstance()
                .handle(() -> {
                    playerDA.update(DataOperation.DELETE, playerDA.getPlayerCopy(selected_player_id));
                    notifyEvent("data_changed", playerDA.getDataContainer());
                },"PlayerControl-delete()", "deletePlayer", ">>>ID: " + selected_player_id);
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
        if(playerDA.isEmpty()){
            notifyLog("player_map_null");
            return;
        }
        new DataSourceChooser(new DataInfo(), this::handleDataSourceForExport);
    }

    private void handleDataSourceForExport(DataInfo targetDataInfo){
        switch (targetDataInfo.getDataType()){
            case FileType ignore -> PlayerExceptionHandler.getInstance()
                    .handle(() -> playerDA.exportFile(targetDataInfo), "PlayerControl-exportFile()", "exportFile");
            case DataSource.DATABASE, DataSource.HIBERNATE, DataSource.OBJECTDB, DataSource.BASEX, DataSource.MONGO -> PlayerExceptionHandler.getInstance()
                    .handle(() -> new DatabaseLogin(playerDA.getDefaultDatabaseInfo(targetDataInfo), this::handleDatabaseLoginForExport),
                            "PlayerControl-exportDB()", "default_database");
            case DataSource.PHP -> PlayerExceptionHandler.getInstance()
                    .handle(() -> playerDA.exportDB(targetDataInfo), "PlayerControl-exportPHP()", "exportPHP");
            default -> throw new IllegalArgumentException("Unknown data source: " + targetDataInfo.getDataType());
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
     * @param dataInfo the {@code DatabaseInfo} object containing the necessary information
     *                     such as database configuration, credentials, and data source details
     *                     required to connect and perform the export operation.
     */
    private void handleDatabaseLoginForExport(DataInfo dataInfo) {
        PlayerExceptionHandler.getInstance()
                .handle(() -> playerDA.exportDB(dataInfo), "PlayerControl-exportDB()", "exportDB");
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
    public void saveToFile(){
        if(playerDA.isSaveToFileNeeded() && playerDA.getDataInfo() != null){
            PlayerExceptionHandler.getInstance()
                    .handle(() -> playerDA.saveAllToFile(), "PlayerControl-save()", "save");
        }
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
            language = switch(TextHandler.fetch().selectionDialog("language")){
                case 0 -> "en";
                case 1 -> "es";
                case 2 -> "cn";
                default -> throw new IllegalStateException("Unexpected language value.");
            };
            TextHandler.fetch().setLanguage(language);
            notifyEvent("language_changed",null);
        } catch (OperationCancelledException e) {
            notifyLog("operation_cancelled");
        }
    }

    /**
     * Adds an event listener to the list of listeners that will be notified of relevant events.
     * The listener will typically define the actions to be performed when an event occurs.
     *
     * @param listener the event listener to be added. It must implement the EventListener interface
     *                 and handle events related to a SortedMap with any key-value pair types.
     */
    public void addListener(EventListener<TreeMap<Integer, VerifiedEntity>> listener){
        listeners.add(listener);
    }

    /**
     * Notifies all registered listeners of a specific event and provides them with associated data.
     * Iterates through the list of EventListener instances and invokes their {@code onEvent} method.
     *
     * @param event the name or identifier of the event to notify about
     * @param data the data associated with the event, passed as a {@code SortedMap} object
     */
    private void notifyEvent(String event, TreeMap<Integer, VerifiedEntity> data){
        listeners.forEach(listener -> listener.onEvent(event, data));
    }

    private void notifyLog(String... message){
        listeners.forEach(listener -> listener.onLog(LogStage.INFO, message));
    }

}
