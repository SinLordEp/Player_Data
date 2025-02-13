package data;

import GUI.Player.PlayerText;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
import data.database.DataBasePlayerCRUD;
import data.database.SqlDialect;
import data.file.FileType;
import data.file.xml_utils;
import data.http.PhpType;
import exceptions.*;
import model.DataInfo;
import model.Player;
import model.Region;
import model.Server;
import org.bson.Document;
import org.hibernate.Session;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.yaml.snakeyaml.Yaml;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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
        DataInfo regionServerInfo = new DataInfo();
        dataInfo.setDataType(DataSource.DATABASE);
        dataInfo.setDialect(SqlDialect.SQLITE);
        PlayerExceptionHandler.getInstance().handle(() -> getDefaultDatabaseInfo(regionServerInfo),
                "PlayerDAO-getDefaultDatabaseInfo()", "default_database");
        region_server_map = PlayerExceptionHandler.getInstance().handle(() -> DataBasePlayerCRUD.readRegionServer(regionServerInfo),
                "PlayerDAO-initializeRegionServer()", "region_server");
    }

    @SuppressWarnings("unchecked")
    public DataInfo getDefaultDatabaseInfo(DataInfo dataInfo) throws ConfigErrorException, IOException {
        dataInfo.setDialect(dataInfo.getDialect());
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
        switch (dataInfo.getDialect()) {
            case MYSQL:
                HashMap<String,Object> mysql_info = (HashMap<String, Object>) default_info.get("MYSQL");
                dataInfo.setUrl((String) mysql_info.get("text_url"));
                dataInfo.setPort((String) mysql_info.get("text_port"));
                dataInfo.setDatabase((String) mysql_info.get("text_database"));
                dataInfo.setUser((String) mysql_info.get("text_user"));
                dataInfo.setPassword((String) mysql_info.get("text_pwd"));
                dataInfo.setTable((String) mysql_info.get("text_table"));
                dataInfo.setQueryRead((String) mysql_info.get("text_query_read"));
                dataInfo.setQueryADD((String) mysql_info.get("text_query_add"));
                dataInfo.setQueryModify((String) mysql_info.get("text_query_modify"));
                dataInfo.setQueryDelete((String) mysql_info.get("text_query_delete"));
                break;
            case SQLITE:
                HashMap<String,Object> sqlite_info = (HashMap<String, Object>) default_info.get("SQLITE");
                dataInfo.setUrl((String) sqlite_info.get("text_url"));
                break;
            case null:
                switch((DataSource)dataInfo.getDataType()){
                    case OBJECTDB:
                        HashMap<String,Object> objectDB_info = (HashMap<String, Object>) default_info.get("OBJECTDB");
                        dataInfo.setUrl((String) objectDB_info.get("text_url"));
                        break;
                    case BASEX:
                        HashMap<String,Object> baseX_info = (HashMap<String, Object>) default_info.get("BASEX");
                        dataInfo.setUrl((String) baseX_info.get("text_url"));
                        dataInfo.setDatabase((String) baseX_info.get("text_database"));
                        break;
                    case MONGO:
                        HashMap<String,Object> mongo_info = (HashMap<String, Object>) default_info.get("MONGO");
                        dataInfo.setUrl((String) mongo_info.get("text_url"));
                        dataInfo.setPort((String) mongo_info.get("text_port"));
                        dataInfo.setDatabase((String) mongo_info.get("text_database"));
                        break;
                    default: throw new OperationException("Unknown database type");
                }
                break;
            default:
                throw new OperationException("Unknown SQL dialect");
        }
        return dataInfo;
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
            PlayerCRUD<DataInfo> currentCRUD = PlayerCRUDFactory.getInstance()
                    .getCRUD(dataInfo)
                    .prepare(dataInfo);
            switch (dataInfo.getDataType()){
                case FileType.TXT -> currentCRUD.read(this::parseArrayListText, null, player_map);
                case FileType.DAT -> currentCRUD.read(this::parseVerifiedEntity, null, player_map);
                case FileType.XML, DataSource.BASEX -> currentCRUD.read(this::parseXmlElement, null, player_map);
                case DataSource.DATABASE -> currentCRUD.read(this::parseResultSet, null, player_map);
                case DataSource.HIBERNATE, DataSource.OBJECTDB -> currentCRUD.read(this::parseList, null, player_map);
                case DataSource.MONGO -> currentCRUD.read(this::parseMongoDocument, null, player_map);
                case DataSource.PHP -> currentCRUD.read(this::parseJsonObject, null, player_map);
                default -> throw new OperationException("Unknown data source: " + dataSource);
            }
            currentCRUD.release();
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
            if (Objects.requireNonNull(dataInfo.getDataType()) instanceof FileType) {
                PlayerCRUD<DataInfo> currentCRUD = PlayerCRUDFactory.getInstance()
                        .getCRUD(dataInfo)
                        .prepare(dataInfo);
                switch ((FileType) dataInfo.getDataType()){
                    case TXT -> currentCRUD.update(this::playerToArrayString, null, player_map);
                    case DAT -> currentCRUD.update(this::playerToArrayEntity, null, player_map);
                    case XML -> currentCRUD.update(this::playerToXmlElement, null, player_map);
                }
                currentCRUD.release();
            }
            isDataChanged = false;
        } catch (Exception e) {
            throw new OperationException(e.getMessage());
        }
    }

    public void update(DataOperation operation, Player player){
        PlayerCRUD<DataInfo> currentCRUD = PlayerCRUDFactory.getInstance()
                .getCRUD(dataInfo)
                .prepare(dataInfo);
        switch(dataInfo.getDataType()){
            case DataSource.DATABASE -> currentCRUD.update(this::playerToUpdateStatement, operation, player);
            case DataSource.HIBERNATE -> currentCRUD.update(this::playerToUpdateSession, operation, player);
            case DataSource.OBJECTDB -> currentCRUD.update(this::playerToUpdateEntityManager, operation, player);
            case DataSource.MONGO -> currentCRUD.update(this::playerToMongoDocument, operation, player);
            case DataSource.BASEX -> currentCRUD.update(this::playerToBaseXQuery, operation, player);
            case PhpType.JSON -> currentCRUD.update(this::playerToJsonObject, operation, player);
            default -> throw new IllegalStateException("Unexpected value: " + dataInfo.getDataType());
        }
        switch (operation){
            case ADD, MODIFY -> player_map.put(player.getID(), player);
            case DELETE -> player_map.remove(player.getID());
        }
        player_map.put(player.getID(), player);
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
    public void exportFile(DataInfo targetDataInfo) {
        String target_extension = getExtension((FileType) targetDataInfo.getDataType());
        String target_path = getPath();
        String target_name = PlayerText.getDialog().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        targetDataInfo.setUrl(target_path);
        try {
            PlayerCRUDFactory.getInstance()
                    .getCRUD(targetDataInfo)
                    .prepare(targetDataInfo);
            //todo
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
    public void exportDB(DataInfo exportDataBaseInfo) {
        TreeMap<Integer, VerifiedEntity> target_player_map = new TreeMap<>();
        PlayerCRUD<DataInfo> currentCRUD = PlayerCRUDFactory.getInstance()
                .getCRUD(exportDataBaseInfo)
                .prepare(exportDataBaseInfo);
        switch (exportDataBaseInfo.getDataType()){
            case DataSource.DATABASE -> currentCRUD.read(this::parseResultSet, null, target_player_map);
            case DataSource.HIBERNATE, DataSource.OBJECTDB -> currentCRUD.read(this::parseList, null, target_player_map);
            case DataSource.MONGO -> currentCRUD.read(this::parseMongoDocument, null, target_player_map);
            case DataSource.BASEX -> currentCRUD.read(this::parseXmlElement, null, player_map);
            case PhpType.JSON -> currentCRUD.read(this::parseJsonObject, null, target_player_map);
            default -> throw new IllegalStateException("Unexpected value: " + exportDataBaseInfo.getDataType());
        }
        for(Map.Entry<Integer, VerifiedEntity> entry: target_player_map.entrySet()){
            if(!player_map.containsKey(entry.getKey())){
                switch (exportDataBaseInfo.getDataType()){
                    case DataSource.DATABASE -> currentCRUD.update(this::playerToUpdateStatement, DataOperation.DELETE, (Player) entry.getValue());
                    case DataSource.HIBERNATE -> currentCRUD.update(this::playerToUpdateSession, DataOperation.DELETE, (Player)entry.getValue());
                    case DataSource.OBJECTDB -> currentCRUD.update(this::playerToUpdateEntityManager, DataOperation.DELETE, (Player)entry.getValue());
                    case DataSource.MONGO -> currentCRUD.update(this::playerToMongoDocument, DataOperation.DELETE, (Player)entry.getValue());
                    case DataSource.BASEX -> currentCRUD.update(this::playerToBaseXQuery, DataOperation.DELETE, (Player)entry.getValue());
                    case PhpType.JSON -> currentCRUD.update(this::playerToJsonObject, DataOperation.DELETE, (Player)entry.getValue());
                    default -> throw new IllegalStateException("Unexpected value: " + exportDataBaseInfo.getDataType());
                }
            }
        }
        for(Map.Entry<Integer, VerifiedEntity> entry: player_map.entrySet()){
            if(target_player_map.containsKey(entry.getKey())){
                switch (exportDataBaseInfo.getDataType()){
                    case DataSource.DATABASE -> currentCRUD.update(this::playerToUpdateStatement, DataOperation.MODIFY, (Player)entry.getValue());
                    case DataSource.HIBERNATE -> currentCRUD.update(this::playerToUpdateSession, DataOperation.MODIFY, (Player)entry.getValue());
                    case DataSource.OBJECTDB -> currentCRUD.update(this::playerToUpdateEntityManager, DataOperation.MODIFY, (Player)entry.getValue());
                    case DataSource.MONGO -> currentCRUD.update(this::playerToMongoDocument, DataOperation.MODIFY, (Player)entry.getValue());
                    case DataSource.BASEX -> currentCRUD.update(this::playerToBaseXQuery, DataOperation.MODIFY, (Player)entry.getValue());
                    case PhpType.JSON -> currentCRUD.update(this::playerToJsonObject, DataOperation.MODIFY, (Player)entry.getValue());
                    default -> throw new IllegalStateException("Unexpected value: " + exportDataBaseInfo.getDataType());
                }
            }else{
                switch (exportDataBaseInfo.getDataType()){
                    case DataSource.DATABASE -> currentCRUD.update(this::playerToUpdateStatement, DataOperation.ADD, (Player)entry.getValue());
                    case DataSource.HIBERNATE -> currentCRUD.update(this::playerToUpdateSession, DataOperation.ADD, (Player)entry.getValue());
                    case DataSource.OBJECTDB -> currentCRUD.update(this::playerToUpdateEntityManager, DataOperation.ADD, (Player)entry.getValue());
                    case DataSource.MONGO -> currentCRUD.update(this::playerToMongoDocument, DataOperation.ADD, (Player)entry.getValue());
                    case DataSource.BASEX -> currentCRUD.update(this::playerToBaseXQuery, DataOperation.ADD, (Player)entry.getValue());
                    case PhpType.JSON -> currentCRUD.update(this::playerToJsonObject, DataOperation.ADD, (Player)entry.getValue());
                    default -> throw new IllegalStateException("Unexpected value: " + exportDataBaseInfo.getDataType());
                }
            }
        }
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

    private void playerToUpdateEntityManager(EntityManager entityManager, DataOperation operation, Player player){
        switch(operation){
            case ADD -> entityManager.persist(player);
            case MODIFY -> entityManager.merge(player);
            case DELETE -> entityManager.remove(player);
        }
    }

    private void parseMongoDocument(Document document, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Player player = new Player();
        player.setID(document.getInteger("id"));
        player.setName(document.getString("name"));
        player.setRegion(new Region(document.getString("region")));
        player.setServer(new Server(document.getString("server"), player.getRegion()));
        dataMap.put(player.getID(), player);
    }

    private void playerToMongoDocument(Document document, DataOperation operation, Player player){
        switch(operation){
            case ADD: document.put("id", player.getID());
                document.put("name", player.getName());
                document.put("region", player.getRegion().getName());
                document.put("server", player.getServer().getName());
                break;
            case MODIFY: document.put("id", player.getID());
                document.put("name", player.getName());
                document.put("region", player.getRegion().getName());
                document.put("server", player.getServer().getName());
                break;
            case DELETE: document.put("id", player.getID());
                break;
        }
    }

    private void parseArrayListText(ArrayList<String> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        list.forEach(text -> {
            String[] player_txt = text.split(";");
            Player player = new Player();
            player.setID(Integer.parseInt(player_txt[0]));
            player.setRegion(new Region(player_txt[1]));
            player.setServer(new Server(player_txt[2], player.getRegion()));
            player.setName(player_txt[3]);
            dataMap.put(player.getID(),player);
        });
    }

    private void playerToArrayString(ArrayList<String> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        for(VerifiedEntity entity : dataMap.values()){
            Player player = (Player) entity;
            list.add("%s;%s;%s;%s".formatted(player.getID(),player.getRegion(),player.getServer(),player.getName()));
        }
    }

    private void parseVerifiedEntity(VerifiedEntity entity, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Player player = (Player) entity;
        dataMap.put(player.getID(),player);
    }

    private void playerToArrayEntity(ArrayList<VerifiedEntity> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        list.addAll(player_map.values());
    }

    private void parseXmlElement(Element element, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        NodeList playerNodes = element.getElementsByTagName("player");
        for (int i = 0; i < playerNodes.getLength(); i++) {
            Node playerNode = playerNodes.item(i);
            if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element playerElement = (Element) playerNode;
                Player player = new Player();
                player.setID(Integer.parseInt(playerElement.getAttribute("id")));
                player.setRegion(new Region(xml_utils.getElementTextContent(playerElement, "region")));
                player.setServer(new Server(xml_utils.getElementTextContent(playerElement, "server"), player.getRegion()));
                player.setName(xml_utils.getElementTextContent(playerElement, "name"));
                dataMap.put(player.getID(), player);
            }
        }
    }

    private void playerToXmlElement(org.w3c.dom.Document document, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        Element rootElement = document.createElement("Player");
        document.appendChild(rootElement);
        for (VerifiedEntity entity : dataMap.values()) {
            Player player = (Player) entity;
            Element playerElement = document.createElement("player");
            playerElement.setAttribute("id", String.valueOf(player.getID()));
            xml_utils.createElementWithText(document, playerElement, "region", player.getRegion().toString());
            xml_utils.createElementWithText(document, playerElement, "server", player.getServer().toString());
            xml_utils.createElementWithText(document, playerElement, "name", player.getName());
            rootElement.appendChild(playerElement);
        }
    }

    private void playerToBaseXQuery(String query, DataOperation operation, VerifiedEntity entity){
        Player player = (Player) entity;
        query = switch (operation){
            case ADD -> "insert node <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player> into /Player"
                    .formatted(player.getID(), player.getRegion(), player.getServer(), player.getName());
            case MODIFY -> "replace node /Player/player[@id='%s'] with <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player>"
                    .formatted(player.getID(), player.getID(), player.getRegion(), player.getServer(), player.getName());
            case DELETE -> "delete node /Player/player[@id='%s']".formatted(player.getID());
        };
    }

    private void parseJsonObject(JSONObject jsonObject, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap){
        JSONArray playersArray = (JSONArray) jsonObject.get("players");
        if(playersArray.isEmpty()) {
            throw new DataCorruptedException("No players found");
        }
        for (Object object : playersArray) {
            Player player = new Player();
            JSONObject playerObject = (JSONObject) object;
            player.setID(Integer.parseInt(playerObject.get("id").toString()));
            player.setName(playerObject.get("name").toString());
            player.setRegion(new Region(playerObject.get("region").toString()));
            player.setServer(new Server(playerObject.get("server").toString(), player.getRegion()));
            dataMap.put(player.getID(), player);
        }
    }

    @SuppressWarnings("unchecked")
    private void playerToJsonObject(JSONObject jsonObject, DataOperation operation, Player player){
        jsonObject.put("id", player.getID());
        jsonObject.put("name", player.getName());
        jsonObject.put("region", player.getRegion().toString());
        jsonObject.put("server", player.getServer().toString());
        jsonObject.put("operation", operation.toString());
    }

}
