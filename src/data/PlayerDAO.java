package data;

import Interface.EntityParser;
import Interface.VerifiedEntity;
import data.database.DatabaseCRUD;
import data.database.SqlDialect;
import exceptions.ConfigErrorException;
import exceptions.DataCorruptedException;
import exceptions.OperationException;
import exceptions.PlayerExceptionHandler;
import model.DataInfo;
import model.Player;
import model.Region;
import model.Server;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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
    private HashMap<Region, Server[]> region_server_map;

    public PlayerDAO(EntityParser entityParser) {
        super(entityParser);
        initializeRegionServer();
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
    @Override
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
    protected void isEntityValid(VerifiedEntity verifiedEntity){
        Player player = (Player) verifiedEntity;
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

    public HashMap<Region, Server[]> getRegion_server_map() {
        return region_server_map;
    }

    public Player getPlayerCopy(int id){
        Player player = (Player) dataContainer.get(id);
        Player playerCopy = new Player();
        playerCopy.setID(id);
        playerCopy.setName(player.getName());
        playerCopy.setRegion(player.getRegion());
        playerCopy.setServer(player.getServer());
        return playerCopy;
    }

    public boolean isSaveToFileNeeded() {
        return isSaveToFileNeeded;
    }

}
