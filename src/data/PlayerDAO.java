package data;

import GUI.Player.PlayerText;
import Interface.GeneralCRUD;
import Interface.VerifiedEntity;
import data.database.DatabaseCRUD;
import data.database.SqlDialect;
import data.file.FileType;
import exceptions.*;
import model.DataInfo;
import model.Player;
import model.Region;
import model.Server;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private boolean isSaveToFileNeeded = false;

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
        DataInfo regionServerInfo = new DataInfo();
        regionServerInfo.setDataType(DataSource.DATABASE);
        regionServerInfo.setDialect(SqlDialect.SQLITE);
        PlayerExceptionHandler.getInstance().handle(() -> getDefaultDatabaseInfo(regionServerInfo),
                "PlayerDAO-getDefaultDatabaseInfo()", "default_database");
        region_server_map = PlayerExceptionHandler.getInstance().handle(() -> DatabaseCRUD.readRegionServer(regionServerInfo),
                "PlayerDAO-initializeRegionServer()", "region_server");
    }

    @SuppressWarnings("unchecked")
    public DataInfo getDefaultDatabaseInfo(DataInfo dataInfo) throws ConfigErrorException {
        HashMap<String, Object> default_info;
        URL resource = getClass().getResource(getProperty("defaultPlayerSQL"));
        if (resource == null) {
            throw new ConfigErrorException("Player default database configuration file not found");
        }
        try(InputStream inputStream = resource.openStream()){
            Yaml yaml = new Yaml();
            default_info = yaml.load(inputStream);
        }catch (IOException e){
            throw new ConfigErrorException("Player default database configuration file cannot be read");
        }
        if(default_info == null){
            throw new ConfigErrorException("Player default database configuration info is empty");
        }
        HashMap<String,Object> database_info = switch (dataInfo.getDialect()) {
            case MYSQL -> (HashMap<String, Object>) default_info.get("MYSQL");
            case SQLITE -> (HashMap<String, Object>) default_info.get("SQLITE");
            case null -> switch((DataSource)dataInfo.getDataType()){
                case OBJECTDB ->(HashMap<String, Object>) default_info.get("OBJECTDB");
                case BASEX -> (HashMap<String, Object>) default_info.get("BASEX");
                case MONGO -> (HashMap<String, Object>) default_info.get("MONGO");
                default ->throw new OperationException("Unknown database type");
            };
            default -> throw new OperationException("Unknown SQL dialect");
        };
        for(Map.Entry<String,Object> entry : database_info.entrySet()){
            switch (entry.getKey()){
                case "text_url" -> dataInfo.setUrl((String) entry.getValue());
                case "text_port" -> dataInfo.setPort((String)entry.getValue());
                case "text_database" -> dataInfo.setDatabase((String) entry.getValue());
                case "text_user" -> dataInfo.setUser((String) entry.getValue());
                case "text_pwd" -> dataInfo.setPassword((String) entry.getValue());
                case "text_table" -> dataInfo.setTable((String) entry.getValue());
                case "text_query_read" -> dataInfo.setQueryRead((String) entry.getValue());
                case "text_query_add" -> dataInfo.setQueryADD((String) entry.getValue());
                case "text_query_modify" -> dataInfo.setQueryModify((String) entry.getValue());
                case "text_query_delete" -> dataInfo.setQueryDelete((String) entry.getValue());
                case "text_query_search" -> dataInfo.setQuerySearch((String) entry.getValue());
            }
        }
        return dataInfo;
    }

    @Override
    public void search(){
        int id = Integer.parseInt(JOptionPane.showInputDialog(null,PlayerText.getDialog().getText("input_id_ongoing")));
        player_map.clear();
        player_map.put(id, null);
        CRUDFactory.getCRUD(dataInfo)
                .prepare()
                .read(PlayerParser.input(dataInfo.getDataType()), DataOperation.SEARCH, player_map)
                .release();
        if(player_map != null && !player_map.isEmpty()){
            isDataValid();
        }else{
            throw new OperationException("No player found");
        }
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
        player_map.clear();
        try {
            CRUDFactory.getCRUD(dataInfo)
                    .prepare()
                    .read(PlayerParser.input(dataInfo.getDataType()), DataOperation.READ, player_map)
                    .release();
            if(player_map != null && !player_map.isEmpty()){
                isDataValid();
            }
        } catch (Exception e) {
            player_map = new TreeMap<>();
            dataInfo = new DataInfo();
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
        if (Objects.requireNonNull(dataInfo.getDataType()) instanceof FileType) {
            CRUDFactory.getCRUD(dataInfo)
                    .prepare()
                    .update(PlayerParser.allOutput(dataInfo.getDataType()), null, player_map)
                    .release();
            isSaveToFileNeeded = false;
        }
    }

    public void update(DataOperation operation, Player player){
        if(!(dataInfo.getDataType() instanceof FileType)){
            CRUDFactory.getCRUD(dataInfo)
                    .prepare()
                    .update(PlayerParser.singleOutput(dataInfo.getDataType()), operation, player)
                    .release();
        }else{
            isSaveToFileNeeded = true;
        }
        switch (operation){
            case ADD, MODIFY -> player_map.put(player.getID(), player);
            case DELETE -> player_map.remove(player.getID());
        }

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
    public void exportFile(DataInfo targetDataInfo) {
        String target_extension = getExtension((FileType) targetDataInfo.getDataType());
        String target_path = getPath();
        String target_name = PlayerText.getDialog().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        createNewFile(target_path);
        targetDataInfo.setUrl(target_path);
        CRUDFactory.getCRUD(targetDataInfo)
                .prepare()
                .update(PlayerParser.allOutput(targetDataInfo.getDataType()), null, player_map)
                .release();
    }

    /**
     * Exports the database using the provided data source.
     * This method utilizes {@code playerDBA.export} to perform the export operation.
     *
     * @param exportDataBaseInfo the database info used for the export operation
     */
    public void exportDB(DataInfo exportDataBaseInfo) {
        TreeMap<Integer, VerifiedEntity> target_player_map = new TreeMap<>();
        GeneralCRUD<DataInfo> currentCRUD = CRUDFactory.getCRUD(exportDataBaseInfo)
                .prepare()
                .read(PlayerParser.input(exportDataBaseInfo.getDataType()),null, target_player_map);
        target_player_map.forEach((current_player_id, verified_entity) -> {
            if(!player_map.containsKey(current_player_id)){
                currentCRUD.update(PlayerParser.singleOutput(exportDataBaseInfo.getDataType()), DataOperation.DELETE, (Player) verified_entity);
            }
        });
        player_map.forEach((current_player_id, verified_entity) -> {
            if(target_player_map.containsKey(current_player_id)){
                currentCRUD.update(PlayerParser.singleOutput(exportDataBaseInfo.getDataType()),DataOperation.MODIFY, (Player) verified_entity);
            }else{
                currentCRUD.update(PlayerParser.singleOutput(exportDataBaseInfo.getDataType()), DataOperation.ADD, (Player) verified_entity);
            }
        });
        currentCRUD.release();
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

    public boolean isSaveToFileNeeded() {
        return isSaveToFileNeeded;
    }

}
