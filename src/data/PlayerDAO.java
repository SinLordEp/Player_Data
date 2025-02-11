package data;

import GUI.Player.PlayerText;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
import data.database.DataBasePlayerCRUD;
import data.database.SqlDialect;
import data.http.PhpType;
import exceptions.*;
import model.DatabaseInfo;
import model.Player;
import model.Region;
import model.Server;
import org.hibernate.Session;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class PlayerDAO extends GeneralDAO {
    private TreeMap<Integer, VerifiedEntity> player_map = new TreeMap<>();
    private HashMap<Region, Server[]> region_server_map;
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
    public PlayerDAO() {
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
        DatabaseInfo info = PlayerExceptionHandler.getInstance().handle(() -> getDefaultDatabaseInfo(SqlDialect.SQLITE),
                "PlayerDAO-getDefaultDatabaseInfo()", "default_database");
        info.setDataSource(DataSource.DATABASE);
        region_server_map = PlayerExceptionHandler.getInstance().handle(() -> DataBasePlayerCRUD.readRegionServer(info),
                "PlayerDAO-initializeRegionServer()", "region_server");
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
    @SuppressWarnings("unchecked")
    public DatabaseInfo getDefaultDatabaseInfo(SqlDialect dialect, DataSource... dataSource) throws ConfigErrorException, IOException {
        DatabaseInfo databaseInfo = new DatabaseInfo();
        databaseInfo.setDialect(dialect);
        HashMap<String, Object> default_info = null;
        URL resource = getClass().getResource(getProperty("defaultPlayerSQL"));
        if (resource != null) {
            try(InputStream inputStream = resource.openStream()){
                Yaml yaml = new Yaml();
                default_info = yaml.load(inputStream);
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
                databaseInfo.setTable((String) mysql_info.get("text_table"));
                databaseInfo.setQueryRead((String) mysql_info.get("text_query_read"));
                databaseInfo.setQueryADD((String) mysql_info.get("text_query_add"));
                databaseInfo.setQueryModify((String) mysql_info.get("text_query_modify"));
                databaseInfo.setQueryDelete((String) mysql_info.get("text_query_delete"));
                break;
            case SQLITE:
                HashMap<String,Object> sqlite_info = (HashMap<String, Object>) default_info.get("SQLITE");
                databaseInfo.setUrl((String) sqlite_info.get("text_url"));
                break;
            case NONE:
                switch(dataSource[0]){
                    case OBJECTDB:
                        HashMap<String,Object> objectDB_info = (HashMap<String, Object>) default_info.get("OBJECTDB");
                        databaseInfo.setUrl((String) objectDB_info.get("text_url"));
                        break;
                    case BASEX:
                        HashMap<String,Object> baseX_info = (HashMap<String, Object>) default_info.get("BASEX");
                        databaseInfo.setUrl((String) baseX_info.get("text_url"));
                        databaseInfo.setDatabase((String) baseX_info.get("text_database"));
                        break;
                    case MONGO:
                        HashMap<String,Object> mongo_info = (HashMap<String, Object>) default_info.get("MONGO");
                        databaseInfo.setUrl((String) mongo_info.get("text_url"));
                        databaseInfo.setPort((String) mongo_info.get("text_port"));
                        databaseInfo.setDatabase((String) mongo_info.get("text_database"));
                        break;
                    default: throw new OperationException("Unknown database type");
                }
                break;
            default:
                throw new OperationException("Unknown SQL dialect");
        }
        if(dataSource.length > 0){
            databaseInfo.setDataSource(dataSource[0]);
            this.databaseInfo = databaseInfo;
        }
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
        try {
            switch (dataSource){
                case NONE :
                    player_map = new TreeMap<>();
                    break;
                case FILE:
                    PlayerCRUDFactory.getInstance()
                            .getCRUD(fileType)
                            .prepare(file_path)
                            .read(this::parse, , player_map)
                            .release();
                    break;
                case DATABASE:
                    PlayerCRUDFactory.getInstance()
                            .getCRUD(DataSource.DATABASE)
                            .prepare(databaseInfo)
                            .read(this::parseResultSet, DataOperation.READ, player_map)
                            .release();
                    break;
                case HIBERNATE, OBJECTDB:
                    PlayerCRUDFactory.getInstance()
                            .getCRUD(dataSource)
                            .prepare(databaseInfo)
                            .read(this::parseList, DataOperation.READ, player_map)
                            .release();
                    break;
                case BASEX, MONGO:

                case PHP:
                    PlayerCRUDFactory.getInstance()
                            .getCRUD()
                            .prepare(phpType)
                            .read(this::parse, , player_map)
                            .release();
                    break;
                default:
                    throw new OperationException("Unknown data source: " + dataSource);
            }
            if(player_map != null && !player_map.isEmpty()){
                isDataValid();
            }
        } catch (Exception e) {
            player_map = new TreeMap<>();
            dataSource = DataSource.NONE;
            throw new OperationException(e.getMessage());
        }
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
        try{
            switch (dataSource){
                case FILE:
                    PlayerCRUDFactory.getInstance()
                            .getCRUD(fileType)
                            .prepare(file_path)
                            .release();
                    break;
                case DATABASE, HIBERNATE, OBJECTDB, MONGO, PHP, BASEX:
                    break;
                default:
                    throw new OperationException("Save: Unknown data source: " + dataSource);
            }
            isDataChanged = false;
        } catch (Exception e) {
            throw new OperationException(e.getMessage());
        }
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
        switch(dataSource){
            case DATABASE:
                PlayerCRUDFactory.getInstance()
                        .getCRUD(dataSource)
                        .prepare(databaseInfo)
                        .update(this::playerToUpdateStatement, DataOperation.ADD, player)
                        .release();
                break;
            case HIBERNATE:
                PlayerCRUDFactory.getInstance()
                        .getCRUD(DataSource.HIBERNATE)
                        .prepare(databaseInfo)
                        .update(this::playerToUpdateSession, DataOperation.ADD, player)
                        .release();
                break;
            case PHP, OBJECTDB, BASEX, MONGO:
        }
        player_map.put(player.getID(), player);
        isDataChanged = true;
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
        switch(dataSource){
            case DATABASE:
                PlayerCRUDFactory.getInstance()
                        .getCRUD(dataSource)
                        .prepare(databaseInfo)
                        .update(this::playerToModifyStatement, DataOperation.MODIFY, player)
                        .release();
                break;
            case HIBERNATE:
                PlayerCRUDFactory.getInstance()
                        .getCRUD(dataSource)
                        .prepare(databaseInfo)
                        .update(this::playerToUpdateSession, DataOperation.MODIFY, player)
                        .release();
                break;
            case PHP, OBJECTDB, MONGO:
                //Always trigger case FILE
            case FILE, BASEX:
                player_map.put(player.getID(), player);
                break;
            default:
                throw new OperationException("Unknown data source: " + dataSource);
        }
        isDataChanged = true;
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
     */
    public void delete(Player player) {
        switch(dataSource){
            case DATABASE:
                PlayerCRUDFactory.getInstance()
                        .getCRUD(dataSource)
                        .prepare(databaseInfo)
                        .update(this::playerToUpdateStatement, DataOperation.DELETE, player)
                        .release();
                break;
            case HIBERNATE:
                PlayerCRUDFactory.getInstance()
                        .getCRUD(DataSource.HIBERNATE)
                        .prepare(databaseInfo)
                        .update(this::playerToUpdateSession, DataOperation.DELETE, player);
            case PHP, OBJECTDB, MONGO:
                //Always trigger case FILE
            case FILE, BASEX:

                break;
            default:
                throw new OperationException("Deletion operation not implemented for this data source");
        }
        isDataChanged = true;
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
        String target_extension = getExtension(fileType);
        String target_path = getPath();
        String target_name = PlayerText.getDialog().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        try {
            PlayerCRUDFactory.getInstance()
                    .getCRUD(fileType)
                    .prepare(target_path);
        } catch (Exception e) {
            throw new FileManageException("Failed to export file with cause: " + e.getMessage());
        }
    }

    /**
     * Exports the database using the provided data source.
     * This method utilizes {@code playerDBA.export} to perform the export operation.
     *
     * @param exportDataBaseInfo the database info used for the export operation
     */
    public void exportDB(DatabaseInfo exportDataBaseInfo) {
        switch (exportDataBaseInfo.getDataSource()){
            case DATABASE -> exportDatabase(exportDataBaseInfo);
            case HIBERNATE -> exportHibernate(exportDataBaseInfo);
        }
    }

    /**
     * Exports the given data type to PHP using the {@code playerPhp.export} method.
     * This method logs an informational message before performing the export operation.
     *
     * @param phpType the data type that needs to be exported
     */
    public void exportPHP(PhpType phpType) {
        PlayerCRUDFactory.getInstance()
                .getCRUD()
                .prepare(phpType);
    }

    public boolean isEmpty(){
        return player_map.isEmpty();
    }

    public TreeMap<Integer, VerifiedEntity> getPlayerMap() {
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
        for(VerifiedEntity player : player_map.values()){
            isPlayerInvalid((Player) player);
        }
    }

    public HashMap<Region, Server[]> getRegion_server_map() {
        return region_server_map;
    }

    public Player getPlayer(int id){
        Player player = (Player) player_map.get(id);
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
        player_map.clear();
    }

    public boolean isDataChanged() {
        return isDataChanged;
    }

    private void parseResultSet(ResultSet resultSet, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        try {
            Player player = new Player();
            player.setID(resultSet.getInt("id"));
            player.setName(resultSet.getString("name"));
            player.setRegion(new Region(resultSet.getString("region")));
            player.setServer(new Server(resultSet.getString("server"), player.getRegion()));
            isPlayerInvalid(player);
            dataMap.put(player.getID(), player);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private void playerToUpdateStatement(PreparedStatement statement, DataOperation operation, Player player){
        try {
            switch (operation){
                case ADD:
                    statement.setInt(1, player.getID());
                    statement.setString(2, player.getRegion().toString());
                    statement.setString(3, player.getServer().toString());
                    statement.setString(4, player.getName());
                    break;
                case MODIFY:
                    statement.setString(1, player.getRegion().toString());
                    statement.setString(2, player.getServer().toString());
                    statement.setString(3, player.getName());
                    statement.setInt(4, player.getID());
                    break;
                case DELETE:
                    statement.setInt(1, player.getID());
                    break;
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    private void exportDatabase(DatabaseInfo exportDataBaseInfo){
        TreeMap<Integer, VerifiedEntity> target_player_map = new TreeMap<>();
        PlayerCRUD<DatabaseInfo> currentCRUD = PlayerCRUDFactory.getInstance()
                .getCRUD(exportDataBaseInfo.getDataSource())
                .prepare(exportDataBaseInfo)
                .read(this::parseResultSet, , target_player_map);
        for(Map.Entry<Integer, VerifiedEntity> entry: target_player_map.entrySet()){
            if(!player_map.containsKey(entry.getKey())){
                currentCRUD.update(this::playerToUpdateStatement, DataOperation.DELETE, entry.getValue());
            }
        }
        for(Map.Entry<Integer, VerifiedEntity> entry: player_map.entrySet()){
            if(target_player_map.containsKey(entry.getKey())){
                currentCRUD.update(this::playerToUpdateStatement, DataOperation.MODIFY, (Player)entry.getValue());
            }else{
                currentCRUD.update(this::playerToUpdateStatement, DataOperation.ADD, (Player)entry.getValue());
            }
        }
        currentCRUD.release();
    }

    private void parseList(List<VerifiedEntity> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        for(VerifiedEntity verifiedEntity : list){
            Player player = (Player) verifiedEntity;
            player_map.put(player.getID(), player);
        }
    }

    private void playerToUpdateSession(Session session, DataOperation operation, Player player){
        switch(operation){
            case ADD -> session.persist(player);
            case MODIFY -> session.merge(player);
            case DELETE -> session.remove(player);
        }
    }

    private void exportHibernate(DatabaseInfo exportDataBaseInfo) {
        TreeMap<Integer, VerifiedEntity> target_player_map = new TreeMap<>();
        PlayerCRUD<DatabaseInfo> currentCRUD = PlayerCRUDFactory.getInstance()
                .getCRUD(exportDataBaseInfo.getDataSource())
                .prepare(exportDataBaseInfo)
                .read(this::parseList, DataOperation.READ, target_player_map);
        for(Map.Entry<Integer, VerifiedEntity> entry: target_player_map.entrySet()){
            if(!player_map.containsKey(entry.getKey())){
                currentCRUD.update(this::playerToUpdateSession, DataOperation.DELETE, (Player)entry.getValue());
            }
        }
        for(Map.Entry<Integer, VerifiedEntity> entry: player_map.entrySet()){
            if(target_player_map.containsKey(entry.getKey())){
                currentCRUD.update(this::playerToUpdateSession, DataOperation.MODIFY, (Player)entry.getValue());
            }else {
                currentCRUD.update(this::playerToUpdateSession, DataOperation.ADD, (Player)entry.getValue());
            }
        }
        currentCRUD.release();
    }

    private void playerToEntityManager(){

    }
}
