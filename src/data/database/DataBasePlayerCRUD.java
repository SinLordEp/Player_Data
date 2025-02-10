package data.database;

import Interface.PlayerCRUD;
import data.DataOperation;
import exceptions.DatabaseException;
import model.DatabaseInfo;
import model.Player;
import model.Region;
import model.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * @author SIN
 */
public class DataBasePlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Connection connection = null;
    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) throws DatabaseException {
        logger.info("Connecting to database with dialect {}", databaseInfo.getDialect());
        try {
            switch (databaseInfo.getDialect()){
                case MYSQL:
                    connection = DriverManager.getConnection("%s:%s/%s".formatted(
                                    databaseInfo.getUrl(),
                                    databaseInfo.getPort(),
                                    databaseInfo.getDatabase()),
                            databaseInfo.getUser(),
                            databaseInfo.getPassword());
                    break;
                case SQLITE:
                    connection = DriverManager.getConnection(databaseInfo.getUrl());
                    break;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to connect via database. Cause: " + e.getMessage());
        }
        if(connection != null){
            logger.info("Success");
            return this;
        }else{
            throw new DatabaseException("Failed to connect via database. Connection is null");
        }
    }

    @Override
    public void release() {
        connection = null;
    }

    /**
     * Reads all player data from the database and returns it as a {@code TreeMap}.
     * This method executes a SQL query to retrieve player records, constructs {@code Player} objects
     * for each record, and maps them by their IDs.
     * It logs the process of reading players from the database and handles any {@code SQLException}
     * encountered during the operation by throwing a {@code DatabaseException}.
     * <p>
     * The method utilizes {@code Player.setID}, {@code Player.setName}, {@code Player.setRegion},
     * and {@code Player.setServer} to populate player attributes. It also creates associated objects
     * like {@code Region} and {@code Server} for each player.
     *
     * @return a {@code TreeMap<Integer, Player>} containing the player data retrieved from the database,
     * where the keys are player IDs and the values are {@code Player} objects.
     */
    @Override
    public PlayerCRUD<DatabaseInfo> read(TreeMap<Integer, Player> player_map) {
        if(connection == null){
            throw new DatabaseException("Database is not connected");
        }
        logger.info("Reading data from database");
        String query = "SELECT * FROM player";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                Player player = new Player();
                player.setID(resultSet.getInt("id"));
                player.setName(resultSet.getString("name"));
                player.setRegion(new Region(resultSet.getString("region")));
                player.setServer(new Server(resultSet.getString("server"), player.getRegion()));
                player_map.put(player.getID(), player);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to read data via database. Cause: "+e.getMessage());
        }
        logger.info("Finished reading from database!");
        return this;
    }

    /**
     * Updates the database with the changes specified in the {@code changed_player_map}.
     * The method performs operations such as adding, modifying, or deleting players
     * in the database based on the mapped {@code DataOperation} for each {@code Player}.
     * It uses a transaction to ensure consistency and rolls back changes in case
     * of an error.
     *
     * @param changed_player_map A map containing {@code Player} objects as keys and
     *                           their corresponding {@code DataOperation} values representing
     *                           the operations to be performed (ADD, MODIFY, DELETE).
     *
     * @throws DatabaseException if the transaction fails or rollback is unsuccessful.
     */
    @Override
    public PlayerCRUD<DatabaseInfo> update(HashMap<Player, DataOperation> changed_player_map) {
        logger.info("Update Database: Updating database...");
        try {
            connection.setAutoCommit(false);
            for(Map.Entry<Player, DataOperation> player_operation : changed_player_map.entrySet()) {
                switch (player_operation.getValue()) {
                    case ADD -> addPlayer(player_operation.getKey());
                    case MODIFY -> modifyPlayer(player_operation.getKey());
                    case DELETE -> deletePlayer(player_operation.getKey());
                }
            }
            connection.commit();
            logger.info("Update Database: Finished!");
            return this;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DatabaseException("Failed to rollback transaction. Cause: "+ex.getMessage());
            }
            throw new DatabaseException("Data rolled back with cause: " + e.getMessage());
        }
    }

    /**
     * Adds a new player to the database. This method inserts the player's details,
     * including their ID, region, server, and name, into the player table in the database.
     * Logs the actions performed during the process and throws an exception
     * if the operation fails due to database issues.
     *
     * @param player The Player object containing the details of the player to be added.
     *               Includes attributes like ID, name, region, and server, which are
     *               required during the database insertion.
     * @throws DatabaseException If the player could not be added due to a database error.
     */
    private void addPlayer(Player player) throws DatabaseException {
        logger.info("Add Player: Adding player with ID: {}", player.getID());
        String query = "INSERT INTO player (id, region, server, name) VALUES (?,?,?,?)";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, player.getID());
            statement.setString(2, player.getRegion().toString());
            statement.setString(3, player.getServer().toString());
            statement.setString(4, player.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add player via database. Cause: " + e.getMessage());
        }
        logger.info("Add Player: Finished adding player!");
    }

    /**
     * Modifies the details of the specified player in the database.
     * This method updates the player's region, server, and name based on
     * the provided {@code Player} object. The operation is logged for tracking.
     *
     * @param player the {@code Player} object containing the updated fields
     *               (region, server, name) and the unique identifier (ID).
     *               The player's ID is used to identify the record to update.
     * @throws DatabaseException if there is a failure to execute the
     *                           database operation due to an SQL error.
     */
    private void modifyPlayer(Player player) throws DatabaseException {
        logger.info("Modify Player: Modifying player with ID: {}", player.getID());
        String query = "UPDATE player SET region = ?, server = ?, name = ? WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, player.getRegion().toString());
            statement.setString(2, player.getServer().toString());
            statement.setString(3, player.getName());
            statement.setInt(4, player.getID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to modify player via database. Cause: " + e.getMessage());
        }
        logger.info("Modify Player: Finished Modifying player!");
    }

    /**
     * Deletes a player from the database.
     * <p>
     * This method removes a player record using the player's ID. It executes a
     * SQL DELETE query to ensure the player is deleted from the database.
     * If there is a failure during the database operation, it throws a
     * {@code DatabaseException}.
     *
     * @param player The {@code Player} object representing the player to be deleted.
     * @throws DatabaseException If an error occurs while executing the delete operation in the database.
     */
    private void deletePlayer(Player player) throws DatabaseException {
        logger.info("Delete Player: Deleting player with ID: {}", player.getID());
        String query = "DELETE FROM player WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, player.getID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete player via database. Cause: " + e.getMessage());
        }
        logger.info("Delete Player: Finished Deleting player!");
    }

    /**
     * Exports player data from the provided {@code player_map} to the target database.
     * Updates the database by synchronizing player data, where it adds, updates, or
     * deletes players as necessary to match the provided data.
     * <p>
     * This method performs the following actions:
     * - Compares the provided player data map with the current database state.
     * - Deletes records in the database that do not exist in the provided {@code player_map}.
     * - Updates records in the database that have matching IDs in the {@code player_map}.
     * - Adds new records for player data in the {@code player_map} that do not exist in the database.
     * <p>
     * Calls {@code read}, {@code deletePlayer}, {@code modifyPlayer}, and {@code addPlayer}
     * for database operations and internal synchronization.
     *
     * @param player_map a {@code TreeMap<Integer, Player>} where the key represents player IDs
     *                   and the value represents player objects to be exported to the database.
     *                   The provided player map reflects the desired state of the database.
     * @throws DatabaseException if an error occurs during any database operation.
     */
    @Override
    public PlayerCRUD<DatabaseInfo> export(TreeMap<Integer, Player> player_map) {
        logger.info("Export Database: Exporting player data...");
        try {
            TreeMap<Integer, Player> target_player_map = new TreeMap<>();
            read(target_player_map);
            for(Integer player_id: target_player_map.keySet()){
                if(!player_map.containsKey(player_id)){
                    deletePlayer(target_player_map.get(player_id));
                }
            }
            for(Integer player_id : player_map.keySet()){
                if(target_player_map.containsKey(player_id)){
                    modifyPlayer(player_map.get(player_id));
                }else{
                    addPlayer(player_map.get(player_id));
                }
            }
            logger.info("Export Database: Finished!");
            return this;
        } catch (DatabaseException e) {
            throw new DatabaseException("Failed to exportFile via database. Cause: " + e.getMessage());
        } catch (Exception e) {
            throw new DatabaseException("Failed to read existed data in database. Cause: " + e.getMessage());
        }
    }

    /**
     * Reads the region-server configuration from the database using a query.
     * The method retrieves regions and their associated servers,
     * mapping each region to an array of servers assigned to it.
     * It uses a Hibernate session to execute the query and processes the results
     * to construct the region-server mapping.
     * This method closes the Hibernate session and disconnects the data source
     * using {@code disconnect(DataSource.HIBERNATE)} after reading the data.
     *
     * @return a {@code HashMap<Region, Server[]>} where each {@code Region} is a key,
     *         mapped to an array of {@code Server} instances that belong to that region.
     */
    public static HashMap<Region, Server[]> readRegionServer(DatabaseInfo databaseInfo) throws DatabaseException {
        HashMap<Region, List<Server>> regionServerListMap = new HashMap<>();
        HashMap<Region, Server[]> regionServerMap = new HashMap<>();
        String query = "SELECT r.name_region AS region_name, s.name_server AS server_name " +
                "FROM region r " +
                "JOIN server s ON r.name_region = s.region";
        try (Connection connection = DriverManager.getConnection(databaseInfo.getUrl(),
                databaseInfo.getUser(),
                databaseInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Region region = new Region(resultSet.getString("region_name"));

                Server server = new Server(resultSet.getString("server_name"), region);

                regionServerListMap.computeIfAbsent(region, _ -> new ArrayList<>()).add(server);
            }

            for (Map.Entry<Region, List<Server>> entry : regionServerListMap.entrySet()) {
                regionServerMap.put(entry.getKey(), entry.getValue().toArray(new Server[0]));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to read region-server data from database. Cause: " + e.getMessage());
        }
        return regionServerMap;
    }


}
