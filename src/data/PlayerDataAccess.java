package data;

import GUI.GeneralText;
import Interface.PlayerDBA;
import Interface.PlayerFDA;
import data.database.HibernateDBA;
import data.database.SqlDialect;
import data.file.FileType;
import data.http.DataType;
import data.http.PlayerPhp;
import exceptions.ConfigErrorException;
import exceptions.DataCorruptedException;
import exceptions.FileManageException;
import exceptions.OperationException;
import model.DatabaseInfo;
import model.Player;
import model.Region;
import model.Server;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * The PlayerDataAccess class handles the management and operations related to player data,
 * including reading, saving, modifying, deleting, and exporting player information across
 * various data sources such as files, databases, and PHP services.
 * <p>
 * This class extends the GeneralDataAccess class and provides functionalities to
 * connect and interact with the underlying data sources while maintaining a local map of player data.
 * @author SIN
 */
public class PlayerDataAccess extends GeneralDataAccess {
    private final HashMap<DataSource, Class<? extends PlayerDBA>> DBAClasses = new HashMap<>();
    private final HashMap<FileType, Class<? extends PlayerFDA>> FDAClasses = new HashMap<>();
    private TreeMap<Integer, Player> player_map = new TreeMap<>();
    private final HashMap<Player, DataOperation> changed_player_map = new HashMap<>();
    private HashMap<Region, Server[]> region_server_map;
    private final PlayerPhp playerPhp;
    private static final Logger logger = LoggerFactory.getLogger(PlayerDataAccess.class);
    private boolean isDataChanged = false;

    /**
     * Constructs a new instance of the PlayerDataAccess class and initializes
     * dependencies required for managing player data. This class is responsible
     * for interacting with various components such as file readers, file writers,
     * database access layers, and PHP export functionality to manage player-related
     * operations across different data sources and formats.
     * <p>
     * During instantiation, the following components are initialized:
     * - PlayerFileReader: Used to read player data from files.
     * - PlayerFileWriter: Used to write player data to files.
     * - PlayerDBA: Provides database access functionalities for player data.
     * - PlayerPhp: Handles PHP-related operations for exporting player data.
     * <p>
     * Logs the initialization process to indicate successful instantiation of the object.
     */
    public PlayerDataAccess() {
        initializeDBA();
        initializeFDA();
        playerPhp = new PlayerPhp();
        logger.info("PlayerDataAccess: Instantiated");
    }

    private void initializeDBA(){
        logger.info("InitializeDBA: Scanning existed DBA classes");
        Reflections reflections = new Reflections("data.database");
        Set<Class<? extends PlayerDBA>> tempDBAClasses = reflections.getSubTypesOf(PlayerDBA.class);
        for(Class<? extends PlayerDBA> tempDBAClass : tempDBAClasses){
            DBAClasses.put(DataSource.fromString(tempDBAClass.getSimpleName().replace("DBA","")), tempDBAClass);
        }
        logger.info("InitializeDBA: Finished.");
    }

    private void initializeFDA(){
        logger.info("InitializeFDA: Scanning existed FDA classes");
        Reflections reflections = new Reflections("data.file");
        Set<Class<? extends  PlayerFDA>> tempFDAClasses = reflections.getSubTypesOf(PlayerFDA.class);
        for(Class<? extends PlayerFDA> tempFDAClass : tempFDAClasses){
            FDAClasses.put(FileType.fromString(tempFDAClass.getSimpleName().replace("FDA","")), tempFDAClass);
        }
        logger.info("InitializeFDA: Finished.");
    }

    /**
     * Initializes the region server by setting up the database connection,
     * retrieving configuration data, and updating the region server map.
     * <p>
     * This method performs the following steps:
     * 1. Logs the start of the initialization process.
     * 2. Retrieves default database information for the SQLite dialect.
     * 3. Configures the database information's data source to use Hibernate.
     * 4. Connects to the database using the configured information.
     * 5. Reads the region server data from the database and updates the
     *    internal region server map.
     * 6. Logs the successful update of the region server map.
     * <p>
     * If any exception occurs during the database configuration or
     * connection process, it wraps the exception in a RuntimeException
     * and terminates the initialization.
     * <p>
     * Method is not a callback but is typically invoked explicitly as
     * part of a broader initialization workflow to ensure the region
     * server state is correctly prepared.
     *
     * @throws RuntimeException if an error occurs while retrieving the
     *                           configuration or connecting to the database.
     */
    public void initializeRegionServer(){
        logger.info("Initializing region server: Reading config file...");
        try {
            DatabaseInfo info = getDefaultDatabaseInfo(SqlDialect.SQLITE);
            info.setDataSource(DataSource.HIBERNATE);
            region_server_map = new HibernateDBA().connect(info).readRegionServer();
        } catch (ConfigErrorException e) {
            throw new RuntimeException(e);
        }
        logger.info("Initializing region server: Region server map updated!");
    }

    /**
     * Retrieves the default database information based on the provided SQL dialect.
     * This method reads the database configuration from a YAML file and populates
     * a {@code DatabaseInfo} object with the necessary details such as URL, port,
     * database name, user, and password. The information changes dynamically based
     * on the specified SQL dialect (e.g., MySQL or SQLite).
     * Throws an exception if the configuration cannot be loaded or if it's missing.
     *
     * @param dialect the {@link SqlDialect} specifying the database type (e.g., MYSQL, SQLITE)
     * @return a {@link DatabaseInfo} object populated with database connection details
     * @throws ConfigErrorException if an error occurs while reading or loading the configuration
     */
    @Override
    @SuppressWarnings("unchecked")
    public DatabaseInfo getDefaultDatabaseInfo(SqlDialect dialect) throws ConfigErrorException {
        logger.info("Get default database info: Reading default database info for dialect: {}", dialect);
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setDialect(dialect);
        HashMap<String, Object> default_info = null;
        URL resource = getClass().getResource(getProperty("defaultPlayerSQL"));
        if (resource != null) {
            try(InputStream inputStream = resource.openStream()){
                Yaml yaml = new Yaml();
                default_info = yaml.load(inputStream);
            }catch (IOException e){
                throw new ConfigErrorException("Error loading default database info");
            }
        }
        if(default_info == null){
            throw new ConfigErrorException("Default database info is null");
        }
        switch (dialect) {
            case MYSQL:
                HashMap<String,Object> mysql_info = (HashMap<String, Object>) default_info.get("MYSQL");
                databaseInfo.setUrl((String) mysql_info.get("text_url"));
                databaseInfo.setPort((String) mysql_info.get("text_port"));
                databaseInfo.setDatabase((String) mysql_info.get("text_database"));
                databaseInfo.setUser((String) mysql_info.get("text_user"));
                databaseInfo.setPassword((String) mysql_info.get("text_pwd"));
                break;
            case SQLITE:
                HashMap<String,Object> sqlite_info = (HashMap<String, Object>) default_info.get("SQLITE");
                databaseInfo.setUrl((String) sqlite_info.get("text_url"));
                break;
            case NONE:
                HashMap<String,Object> objectDB_info = (HashMap<String, Object>) default_info.get("OBJECTDB");
                databaseInfo.setUrl((String) objectDB_info.get("text_url"));
                break;
        }
        logger.info("Get default database info: Finished reading database info!");
        return databaseInfo;
    }

    /**
     * Reads player data from the specified {@code dataSource}. This method determines
     * the type of the data source and delegates the reading operation to the appropriate
     * mechanism. It processes data containing information about players from various
     * sources such as files, databases, and PHP endpoints. Handles validation of the
     * retrieved data to ensure integrity.
     *
     * <p>Operational Workflow:
     * - If {@code dataSource} is {@code NONE}, resets the {@code player_map} to an
     *   empty {@code TreeMap}.
     * - If {@code dataSource} is {@code FILE}, invokes {@code fileReader.read} passing
     *   the {@code fileType} and {@code file_path}.
     * - If {@code dataSource} is {@code DATABASE} or {@code HIBERNATE}, delegates the
     *   reading operation to {@code playerDBA.read}.
     * - If {@code dataSource} is {@code PHP}, retrieves the data using
     *   {@code playerPhp.read} with the specified {@code dataType}.
     *
     * <p>After successfully reading data, the {@code isDataValid} method validates
     * the contents of {@code player_map}. If the reading process fails, {@code player_map}
     * is reset to an empty {@code TreeMap}, the {@code dataSource} is reset to
     * {@code DataSource.NONE}, and an {@code OperationException} with details about the
     * error is thrown.
     * <p>
     * Logs detailed information about each step of the reading process, including the
     * start, delegation to specific handlers, validation process, and successful completion.
     *
     * @throws OperationException if any error occurs during the data reading process.
     */
    @Override
    public void read() {
        logger.info("Read: Reading data from {}", dataSource);
        try {
            switch (dataSource){
                case NONE :
                    logger.info("Read: Resetting player map...");
                    player_map = new TreeMap<>();
                    break;
                case FILE:
                    logger.info("Read: Checking if file is accessible");
                    File file = new File(file_path);
                    if(isFileAccessible(file)){
                        logger.info("Read: File is accessible, reading file...");
                        player_map = FDAClasses.get(fileType)
                                .getDeclaredConstructor()
                                .newInstance()
                                .read(file);
                    }else{
                        throw new FileManageException("File is not accessible");
                    }
                    break;
                case DATABASE, HIBERNATE, OBJECTDB:
                    logger.info("Read: Calling DBA...");
                    player_map = DBAClasses.get(dataSource)
                            .getDeclaredConstructor()
                            .newInstance()
                            .connect(databaseInfo)
                            .read();
                    break;
                case PHP:
                    logger.info("Read: Calling PHP...");
                    player_map = playerPhp.read(dataType);
                    break;
            }
            if(player_map != null && !player_map.isEmpty()){
                isDataValid();
            }
        } catch (Exception e) {
            player_map = new TreeMap<>();
            dataSource = DataSource.NONE;
            throw new OperationException("Read: Failed to read data with cause: " + e.getMessage());
        }
        logger.info("Read: Finished reading data!");
    }

    /**
     * Saves the current player data to the configured data source. This method determines
     * the data source type and delegates the save operation to the appropriate handler. It
     * manages saving data to files, databases, or PHP endpoints based on the availability
     * of modified data in the {@code changed_player_map}.
     * <p>
     * If no changes exist in the {@code changed_player_map} and the data source is not a
     * file, the method exits early as no save operation is required. Otherwise, the method
     * processes and saves the data using specialized methods for the specified data source.
     * <p>
     * Operational Workflow:
     * - If the data source is {@code FILE}, calls {@code fileWriter.write} to write data to a file.
     * - If the data source is {@code DATABASE} or {@code HIBERNATE}, performs the following:
     *   1. Establishes a database connection using {@code playerDBA.connect}.
     *   2. Updates the database data by calling {@code playerDBA.update}.
     *   3. Disconnects from the database using {@code playerDBA.disconnect}.
     *   4. Clears the {@code changed_player_map} after a successful update.
     * - If the data source is {@code PHP}, updates data using {@code playerPhp.update} and then
     *   clears the {@code changed_player_map}.
     * <p>
     * Exception Handling:
     * - If an exception occurs during any stage of the save operation, the method wraps the
     *   exception into an {@code OperationException} and throws it with a descriptive error
     *   message.
     * <p>
     * Logs detailed information throughout the save process, including the start of saving,
     * delegation to specific handlers, successful completion, and any issues encountered.
     * <p>
     * This method is not a callback but is invoked explicitly for saving player-related data
     * to the selected data source.
     *
     * @throws OperationException if an error occurs during the save operation.
     */
    @Override
    public void save(){
        logger.info("Save: Saving data to {}", dataSource);
        try{
            switch (dataSource){
                case FILE:
                    File file = new File(file_path);
                    if(isFileAccessible(file)){
                        FDAClasses.get(fileType)
                                .getDeclaredConstructor()
                                .newInstance()
                                .write(file, player_map);
                    }else {
                        throw new FileManageException("File is not accessible");
                    }
                    break;
                case DATABASE, HIBERNATE, OBJECTDB:
                    DBAClasses.get(dataSource)
                            .getDeclaredConstructor()
                            .newInstance()
                            .connect(databaseInfo)
                            .update(changed_player_map);
                    changed_player_map.clear();
                    break;
                case PHP:
                    playerPhp.update(changed_player_map);
                    changed_player_map.clear();
                    break;
            }
        } catch (Exception e) {
            throw new OperationException("Failed to save data with cause: " + e.getMessage());
        }
        isDataChanged = false;
        logger.info("Save: Finished saving data!");
    }

    /**
     * Adds a player to the appropriate data structure based on the current data source.
     * This method manages player addition by updating necessary collections and ensuring
     * consistency across different configurations such as FILE, DATABASE, HIBERNATE, or PHP.
     * <p>
     * If the data source is {@code DATABASE}, {@code HIBERNATE}, or {@code PHP}, the player is added
     * to {@code changed_player_map} with the operation {@code DataOperation.ADD}.
     * For the {@code FILE} data source, or in addition to other cases, the player is added
     * to {@code player_map}.
     * <p>
     * Logs detailed information during the addition process, including the start,
     * operations performed, and successful completion of the player addition.
     *
     * @param player the {@code Player} object to add to the system, containing the
     *               relevant player details and unique identifier.
     */
    public void add(Player player) {
        logger.info("Add: Adding player with ID: {}", player.getID());
        switch(dataSource){
            case DATABASE, HIBERNATE, PHP, OBJECTDB:
                logger.info("Add: Adding player to changed player map");
                changed_player_map.put(player, DataOperation.ADD);
        }
        logger.info("Add: Adding player to current player map");
        player_map.put(player.getID(), player);
        isDataChanged = true;
        logger.info("Add: Finished adding player!");
    }

    /**
     * Modifies the provided player based on the configured data source. This method
     * updates internal mappings to reflect the changes made to the player.
     *
     * @param player the {@code Player} object to modify. The method uses the player's ID
     *               for logging and updating relevant data structures.
     *               Ensure the {@code Player} object is valid and contains a proper ID.
     */
    public void modify(Player player) {
        logger.info("Modify: Modifying player with ID: {}", player.getID());
        switch(dataSource){
            case DATABASE, HIBERNATE, PHP, OBJECTDB:
                logger.info("Modify: Adding modified player with ID: {} to changed player map", player.getID());
                changed_player_map.put(player, DataOperation.MODIFY);
                //Always trigger case FILE
            case FILE:
                logger.info("Modify: Adding modified player with ID: {} to current player map", player.getID());
                player_map.put(player.getID(), player);
                break;
        }
        isDataChanged = true;
        logger.info("Modify: Finished modifying player!");
    }

    /**
     * Deletes a player based on the provided player ID. This method performs
     * deletion operations depending on the current {@code dataSource}. It handles
     * database-related deletion by logging the operation, updating the
     * {@code changed_player_map}, and removing the player from {@code player_map}.
     * <p>
     * For {@code dataSource} scenarios such as FILE, the method directly removes
     * the player from {@code player_map}.
     *
     * @param selected_player_id the ID of the player to be deleted
     */
    public void delete(int selected_player_id) {
        logger.info("Delete: Deleting player with ID: {}", selected_player_id);
        switch(dataSource){
            case DATABASE, HIBERNATE, PHP, OBJECTDB:
                logger.info("Delete: Adding deleted player with ID: {} to changed player map", selected_player_id);
                changed_player_map.put(player_map.get(selected_player_id), DataOperation.DELETE);
                //Always trigger case FILE
            case FILE:
                logger.info("Delete: Deleting player with ID: {} from current player map", selected_player_id);
                player_map.remove(selected_player_id);
                break;
        }
        isDataChanged = true;
        logger.info("Delete: Finished deleting player!");
    }

    /**
     * Exports a file containing player data to a specified location defined by user input.
     * This method constructs the file path, including file name and extension, based on the user-provided input
     * and writes the data to the file system using the {@code fileWriter.write()} method.
     * Logs the process at different stages for traceability.
     * <p>
     * Steps performed by the method:
     * - Retrieves the appropriate file extension using {@code getExtension()} based on the file type.
     * - Computes the target path location using {@code getPath()} and user input for the file name.
     * - Appends the file name and extension to the target path.
     * - Writes the player data to the target path using the {@code fileWriter.write()} method.
     * - Logs progress and success messages during the file export process.
     */
    public void exportFile() {
        logger.info("Export file: Building extension...");
        String target_extension = getExtension(fileType);
        String target_path = getPath();
        String target_name = GeneralText.getDialog().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        logger.info("Export file: Target path is set to {}", target_path);
        try {
            File target_file = new File(target_path);
            if(isFileAccessible(target_file)){
                FDAClasses.get(fileType)
                        .getDeclaredConstructor()
                        .newInstance()
                        .write(target_file,player_map);
            }else{
                throw new FileManageException("File is not accessible");
            }
        } catch (Exception e) {
            throw new FileManageException("Failed to export file with cause: " + e.getMessage());
        }
        logger.info("Export file: Finished exporting file!");
    }

    /**
     * Exports the database using the provided data source.
     * This method utilizes {@code playerDBA.export} to perform the export operation.
     *
     * @param exportDataBaseInfo the database info used for the export operation
     */
    public void exportDB(DatabaseInfo exportDataBaseInfo) {
        logger.info("Export DB: Calling DBA...");
        try {
            DBAClasses.get(dataSource)
                    .getDeclaredConstructor()
                    .newInstance()
                    .connect(exportDataBaseInfo)
                    .export(player_map);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Exports the given data type to PHP using the {@code playerPhp.export} method.
     * This method logs an informational message before performing the export operation.
     *
     * @param dataType the data type that needs to be exported
     */
    public void exportPHP(DataType dataType) {
        logger.info("Export PHP: Calling PHP...");
        playerPhp.export(dataType, player_map);
    }

    private boolean isFileAccessible(File file) {
        return file.isFile() && file.canRead() && file.canWrite();
    }

    public boolean isEmpty(){
        return player_map.isEmpty();
    }

    public TreeMap<Integer, Player> getPlayerMap() {
        return player_map;
    }

    /**
     * Validates the given {@code Player} object to ensure that all its attributes
     * and associated mappings are valid. Throws {@code DataCorruptedException}
     * if any validation fails.
     * <p>
     * The method performs the following checks:
     * 1. The {@code region_server_map} is not empty.
     * 2. The {@code region_server_map} contains the player's region.
     * 3. The player's server is valid within their associated region.
     * 4. The player's ID is a positive value.
     * 5. The player's name is not blank.
     *
     * @param player the {@code Player} object whose attributes need to be validated.
     *               It includes details such as region, server, ID, and name.
     * @throws DataCorruptedException if any of the validation checks fail.
     */
    private void isPlayerInvalid(Player player){
        if(region_server_map.isEmpty()){
            throw new DataCorruptedException("region_server_map is null");
        }
        if(!region_server_map.containsKey(player.getRegion())){
            throw new DataCorruptedException("Player's region is not found");
        }
        boolean server_valid = false;
        for(Server server : region_server_map.get(player.getRegion())){
            if (server.equals(player.getServer())) {
                server_valid = true;
                break;
            }
        }
        if(!server_valid){
            throw new DataCorruptedException("Player's server is not found");
        }
        if(player.getID() <= 0){
            throw new DataCorruptedException("Player's ID is invalid");
        }
        if(player.getName().isBlank()){
            throw new DataCorruptedException("Player's name is invalid");
        }
    }

    /**
     * Validates the imported data by checking each player in the {@code player_map}.
     * Logs the validation process, ensuring all players' data is verified using the
     * {@code isPlayerInvalid} method.
     * <p>
     * This method iterates through the values of {@code player_map},
     * passes each {@code Player} object to the {@code isPlayerInvalid} method for validation,
     * and logs messages before and after the validation process.
     */
    private void isDataValid(){
        logger.info("isDataValid: Validating imported data...");
        for(Player player : player_map.values()){
            isPlayerInvalid(player);
        }
        logger.info("isDataValid: Data is valid");
    }

    public HashMap<Region, Server[]> getRegion_server_map() {
        return region_server_map;
    }

    public Player getPlayer(int id){
        Player player = player_map.get(id);
        Player playerCopy = new Player();
        playerCopy.setID(id);
        playerCopy.setName(player.getName());
        playerCopy.setRegion(player.getRegion());
        playerCopy.setServer(player.getServer());
        return playerCopy;
    }

    /**
     * Clears all current data related to the players.
     * This method logs the operation at the start and completion for tracking purposes.
     * First, it logs a message indicating that player data is being cleared.
     * Then, it clears the {@code player_map} using its {@code clear} method.
     * Finally, it logs a message confirming successful clearing of the data.
     */
    public void clearData(){
        logger.info("Clearing current data...");
        player_map.clear();
        logger.info("Player data is cleared successfully");
    }

    public boolean isDataChanged() {
        return isDataChanged;
    }
}
