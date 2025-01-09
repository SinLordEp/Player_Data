package data.database;

import Interface.GeneralDBA;
import data.DataOperation;
import data.DataSource;
import exceptions.DatabaseException;
import exceptions.ObjectDBException;
import model.DatabaseInfo;
import model.Player;
import model.Region;
import model.Server;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.net.URL;
import java.sql.*;
import java.util.*;

import static main.principal.getProperty;

/**
 * The {@code PlayerDBA} class is a data access object designed to manage
 * interactions with a database for {@code Player} entities. It extends
 * the {@code GeneralDBA} class, providing database operations specifically
 * tailored for {@code Player} and utilizing a {@code TreeMap} for storage
 * and retrieval.
 * <p>
 * The class supports both direct database connections and Hibernate ORM
 * configurations for flexibility in data access and manipulation.
 * @author SIN
 */
public class PlayerDBA implements GeneralDBA<TreeMap<Integer, Player>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayerDBA.class);
    private final Configuration configuration = new Configuration();
    private SessionFactory sessionFactory = null;
    private Connection connection = null;
    private EntityManager entityManager = null;
    /**
     * Instantiates a new {@code PlayerDBA} object and configures the Hibernate framework.
     * This constructor initializes the necessary configuration settings for interacting with
     * the database through Hibernate. It reads the Hibernate configuration file using a
     * property key and prepares the Hibernate session factory for usage.
     * <p>
     * The method performs the following steps:
     * - Logs the instantiation of the {@code PlayerDBA} class.
     * - Retrieves the Hibernate configuration file path using {@code getProperty}.
     * - Configures the Hibernate {@code Configuration} instance with the specified resource.
     * <p>
     * This constructor is essential to set up the database access layer, leveraging Hibernate
     * for managing persistence and object-relational mapping.
     */
    public PlayerDBA()  {
        logger.info("PlayerDBA: Instantiated");
        URL resource = getClass().getResource(getProperty("hibernateConfig"));
        configuration.configure(resource);
    }

    /**
     * Connects to a database using the provided {@code DatabaseInfo}.
     * This method determines the connection type (e.g., direct database connection
     * or Hibernate session) based on the {@code dataSource} specified in the
     * {@code DatabaseInfo} object. It delegates the connection process to either
     * {@code connectDatabase} or {@code connectHibernate} and logs the connection activity.
     *
     * @param databaseInfo the information required to establish a connection,
     *                     including the database URL, user credentials, and
     *                     connection type (e.g., {@code DataSource.DATABASE} or
     *                     {@code DataSource.HIBERNATE}).
     * @return {@code true} if the connection is successfully established;
     *         throws an {@code IllegalStateException} for unsupported data sources
     *         or other connection failures.
     * @throws IllegalStateException if the {@code DataSource} type in {@code databaseInfo}
     *                                is unsupported.
     */
    @Override
    public boolean connect(DatabaseInfo databaseInfo) {
        logger.info("Connect: Connecting to database via {}", databaseInfo.getUrl());
        return switch (databaseInfo.getDataSource()){
            case DATABASE -> connectDatabase(databaseInfo);
            case HIBERNATE -> connectHibernate(databaseInfo);
            case OBJECTDB -> connectObjectDB(databaseInfo);
            default -> throw new IllegalStateException("Unexpected value: " + databaseInfo.getDataSource());
        };
    }

    /**
     * Establishes a direct connection to a database using the provided {@code DatabaseInfo}.
     * The method determines the database dialect (e.g., {@code MYSQL}, {@code SQLITE}),
     * and connects accordingly by using the relevant credentials and configuration
     * parameters defined in the {@code DatabaseInfo} object. It logs the connection
     * activity and throws a {@code DatabaseException} if the connection fails.
     *
     * @param databaseInfo the information required to establish the database connection,
     *                     including the dialect, URL, port, database name, username,
     *                     and password.
     * @return {@code true} if the connection is successfully established; otherwise,
     *         returns {@code false}.
     * @throws DatabaseException if the connection fails due to invalid credentials,
     *                           incorrect configuration, or other SQL errors.
     */
    private boolean connectDatabase(DatabaseInfo databaseInfo) {
        logger.info("Connect database: Connecting to database with dialect {}", databaseInfo.getDialect());
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
        logger.info("Connect database: Success");
        return connection != null;
    }

    /**
     * Establishes a Hibernate-based connection to a database using the provided {@code DatabaseInfo}.
     * The method determines the SQL dialect (e.g., {@code MYSQL}, {@code SQLITE}) and configures the
     * connection parameters accordingly, including database URL, username, and password. It attempts
     * to build the Hibernate session factory and verifies if the session is successfully opened.
     * Logs the connection activity and throws a {@code DatabaseException} if there is an error during
     * the connection setup.
     *
     * @param databaseInfo the information required to establish the Hibernate connection,
     *                     including the SQL dialect, database URL, port, database name,
     *                     username, and password.
     * @return {@code true} if the Hibernate session is successfully opened;
     *         otherwise throws a {@code DatabaseException}.
     * @throws DatabaseException if the connection fails due to errors in configuration,
     *                           invalid credentials, or exceptions during session factory creation.
     */
    private boolean connectHibernate(DatabaseInfo databaseInfo) {
        logger.info("Connect hibernate: Connecting to database with dialect {}", databaseInfo.getDialect());
        switch (databaseInfo.getDialect()){
            case MYSQL:
                setURL("%s:%s/%s".formatted(
                        databaseInfo.getUrl(),
                        databaseInfo.getPort(),
                        databaseInfo.getDatabase()));
                setUser(databaseInfo.getUser());
                setPassword(databaseInfo.getPassword());
                break;
            case SQLITE:
                setURL(databaseInfo.getUrl());
                break;
        }
        try {
            sessionFactory = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            throw new DatabaseException("Failed to connect via hibernate. Cause: " + e.getMessage());
        }
        logger.info("Connect hibernate: Success");
        return sessionFactory.isOpen();
    }

    private boolean connectObjectDB(DatabaseInfo databaseInfo) {
        logger.info("Connect ObjectDB: Connecting to ObjectDB file server");
        try{
            entityManager = Persistence.createEntityManagerFactory(databaseInfo.getUrl()).createEntityManager();
        }catch (Exception e){
            throw new ObjectDBException("Failed to connect via ObjectDB. Cause: " + e.getMessage());
        }
        logger.info("Connect ObjectDB: Success");
        return entityManager.isOpen();
    }

    /**
     * Disconnects from the specified {@code DataSource}.
     * This method clears resources associated with the given {@code DataSource},
     * such as setting the database connection or Hibernate session factory to {@code null}.
     * It logs the disconnection process and ensures that resources are properly released.
     *
     * @param dataSource the source from which to disconnect, e.g.,
     *                   {@code DataSource.DATABASE} or {@code DataSource.HIBERNATE}.
     */
    public void disconnect(DataSource dataSource){
        logger.info("Disconnect: Disconnecting from database via {}", dataSource);
        switch (dataSource){
            case DATABASE:
                connection = null;
                break;
            case HIBERNATE:
                sessionFactory = null;
                break;
            case OBJECTDB:
                entityManager.close();
                entityManager = null;
                break;
        }
        logger.info("Disconnect: Success");
    }

    /**
     * Reads all player data from the specified {@code DataSource}.
     * Depending on the given {@code dataSource}, this method delegates the reading process
     * to either {@code readDatabase()} or {@code readHibernate()} and retrieves
     * player data accordingly. After reading, the method disconnects from the source using {@code disconnect(DataSource)}.
     *
     * @param dataSource the source to read the data from, which can be either {@code DataSource.DATABASE}
     *                   or {@code DataSource.HIBERNATE}.
     * @return a {@code TreeMap<Integer, Player>} containing the player data,
     *         where the keys are player IDs and the values are {@code Player} objects.
     */
    @Override
    public TreeMap<Integer, Player> read(DataSource dataSource) {
        logger.info("Read: Reading from database via {}", dataSource);
        TreeMap<Integer, Player> player_map = switch (dataSource){
            case DATABASE -> readDatabase();
            case HIBERNATE -> readHibernate();
            case OBJECTDB -> readObjectDB();
            default -> null;
        };
        disconnect(dataSource);
        logger.info("Read: Finished reading from database!");
        return player_map;
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
    public HashMap<Region, Server[]> readRegionServer(){
        logger.info("Read Region server: Reading region server config");
        HashMap<Region, Server[]> regionServerMap = new HashMap<>();
        String hql = "SELECT s.region, s FROM Server s";
        try(Session session = sessionFactory.openSession()){
            HashMap<Region, List<Server>> regionServerListMap = new HashMap<>();
            session.beginTransaction();
            List<Object[]> results = session.createQuery(hql, Object[].class).getResultList();
            for (Object[] result : results) {
                Region region = (Region) result[0];
                Server server = (Server) result[1];
                // if region does not exist in map, it will create one
                regionServerListMap.computeIfAbsent(region, _ -> new ArrayList<>()).add(server);
            }
            for (HashMap.Entry<Region, List<Server>> entry : regionServerListMap.entrySet()) {
                Region region = entry.getKey();
                regionServerMap.put(region, entry.getValue().toArray(new Server[0]));
            }
        }
        disconnect(DataSource.HIBERNATE);
        return regionServerMap;
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
     * @throws DatabaseException if there are issues reading from the database, such as SQL errors.
     */
    private TreeMap<Integer, Player> readDatabase() {
        logger.info("Read Database: Reading data from database");
        TreeMap<Integer, Player> player_map = new TreeMap<>();
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
        logger.info("Read Database: Finished reading from database!");
        return player_map;
    }

    /**
     * Reads all player data from the database using Hibernate and returns it as a {@code TreeMap}.
     * This method utilizes a Hibernate session to execute an HQL query that retrieves {@code Player}
     * objects along with their associated {@code Region} and {@code Server} entities.
     * Each {@code Player} object is mapped by its ID in the resulting {@code TreeMap}.
     * <p>
     * The method logs the start and end of the reading process and throws a {@code DatabaseException}
     * if any issue arises during the operation. It ensures that the Hibernate session is properly
     * managed within a try-with-resources block.
     *
     * @return a {@code TreeMap<Integer, Player>} containing the player data read from the database,
     *         where the keys are player IDs, and the values are {@code Player} objects.
     * @throws DatabaseException if an error occurs while reading data through Hibernate.
     */
    private TreeMap<Integer, Player> readHibernate() {
        logger.info("Read Hibernate: Reading data from database");
        TreeMap<Integer, Player> player_map = new TreeMap<>();
        try (Session session = sessionFactory.openSession()) {
            String HQL = "FROM Player p LEFT JOIN FETCH p.region LEFT JOIN FETCH p.server";
            List<Player> list = session.createQuery(HQL, Player.class).getResultList();
            for(Player player : list){
                player_map.put(player.getID(), player);
            }
        }catch (Exception e){
            throw new DatabaseException("Failed to read data via hibernate. Cause: " + e.getMessage());
        }
        logger.info("Read Hibernate: Finished reading from database!");
        return player_map;
    }

    private TreeMap<Integer, Player> readObjectDB() {
        logger.info("Read ObjectDB: Reading data from ObjectDB file");
        TreeMap<Integer, Player> player_map = new TreeMap<>();
        try{
            entityManager.getTransaction().begin();
            TypedQuery<Player> query = entityManager.createQuery("SELECT s FROM Player s", Player.class);
            if(!query.getResultList().isEmpty()){
                List<Player> playerList = query.getResultList();
                for(Player player : playerList){
                    player_map.put(player.getID(), player);
                }
            }
        }catch (Exception e){
            throw new ObjectDBException("Failed to read data via ObjectDB. Cause: "+e.getMessage());
        }
        return player_map;
    }

    /**
     * Updates the database or Hibernate data source with the provided changes in the player map.
     * This method evaluates the specified {@code DataSource} and delegates the update process
     * to either {@code updateDatabase} or {@code updateHibernate}. It logs the start and end
     * of the update operation.
     *
     * @param dataSource the data source to update, either {@code DataSource.DATABASE} or {@code DataSource.HIBERNATE}.
     * @param changed_player_map a {@code HashMap} containing players as keys and their corresponding
     *                           {@code DataOperation} as values, indicating the operation to perform
     *                           on each player (e.g., add, modify, delete).
     */
    public void update(DataSource dataSource, HashMap<Player,DataOperation> changed_player_map) {
        logger.info("Update: Updating database data with changed player map...");
        switch (dataSource){
            case DATABASE -> updateDatabase(changed_player_map);
            case HIBERNATE -> updateHibernate(changed_player_map);
            case OBJECTDB -> updateObjectDB(changed_player_map);
        }
        logger.info("Update: Finished updating database!");
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
    private void updateDatabase(HashMap<Player,DataOperation> changed_player_map) {
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
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DatabaseException("Failed to rollback transaction. Cause: "+ex.getMessage());
            }
            throw new DatabaseException("Data rolled back with cause: " + e.getMessage());
        }
        logger.info("Update Database: Finished!");
    }

    /**
     * Updates the database using Hibernate operations based on the specified map of players and their corresponding
     * {@code DataOperation}. This method processes each entry in the provided map and performs the appropriate Hibernate action
     * (add, modify, or delete) for each player. The method commits the transaction upon successful execution or rolls back changes
     * in case of failure. After execution, the input map will be cleared.
     *
     * @param changed_player_map a map containing {@code Player} objects as keys and {@code DataOperation} values indicating the
     *                           action to apply (ADD, MODIFY, or DELETE) for each player in the database
     * @throws DatabaseException if there is an error during the database update process
     */
    private void updateHibernate(HashMap<Player,DataOperation> changed_player_map) {
        logger.info("Update Hibernate: Updating database...");
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            for(Map.Entry<Player, DataOperation> player_operation : changed_player_map.entrySet()) {
                switch (player_operation.getValue()) {
                    case ADD -> session.persist(player_operation.getKey());
                    case MODIFY -> session.merge(player_operation.getKey());
                    case DELETE -> session.remove(player_operation.getKey());
                }
            }
            transaction.commit();
        }catch(Exception e){
            logger.error("Update Hibernate: Failed to update database, rollback data. Cause: {}", e.getMessage());
            if(transaction != null){
                transaction.rollback();
                logger.info("Update Hibernate: Data is rollback");
            }
            throw new DatabaseException("Failed to modify player via Hibernate. Cause: " + e.getMessage());
        }
        logger.info("Update Hibernate: Finished!");
    }

    private void updateObjectDB(HashMap<Player, DataOperation> changedPlayerMap) {
        logger.info("Update ObjectDB: Updating database...");
        try {
            entityManager.getTransaction().begin();
            for(Map.Entry<Player, DataOperation> player_operation : changedPlayerMap.entrySet()) {
                switch (player_operation.getValue()) {
                    case ADD -> entityManager.persist(player_operation.getKey());
                    case MODIFY -> entityManager.merge(player_operation.getKey());
                    case DELETE -> entityManager.remove(player_operation.getKey());
                }
            }
            entityManager.getTransaction().commit();
        }catch(Exception e){
            logger.error("Update ObjectDB: Failed to update database, rollback data. Cause: {}", e.getMessage());
            if(entityManager.getTransaction() != null){
                entityManager.getTransaction().rollback();
            }
        }
        logger.info("Update ObjectDB: Finished!");
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
     * Exports player data using the specified data source. This method manages
     * the export process by delegating to specific export methods based on the
     * data source type and handles disconnection after the export.
     *
     * @param dataSource the data source to use for exporting player data, such as DATABASE or HIBERNATE
     * @param player_map a mapping of player IDs to {@code Player} objects to be exported
     */
    public void export(DataSource dataSource, TreeMap<Integer,Player> player_map) {
        logger.info("Export: Exporting player data via {}", dataSource);
        switch (dataSource){
            case DATABASE -> exportDatabase(player_map);
            case HIBERNATE -> exportHibernate(player_map);
            case OBJECTDB -> exportObjectDB(player_map);
        }
        disconnect(dataSource);
        logger.info("Export: Finished exporting player data!");
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
    private void exportDatabase(TreeMap<Integer,Player> player_map) {
        logger.info("Export Database: Exporting player data...");
        TreeMap<Integer, Player> target_player_map = readDatabase();
        try {
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
        } catch (DatabaseException e) {
            throw new DatabaseException("Failed to exportFile via database. Cause: " + e.getMessage());
        }
        logger.info("Export Database: Finished!");
    }

    /**
     * Exports player data to Hibernate by synchronizing it between the given {@code player_map}
     * and the database. It removes any players from the database that are not present
     * in the {@code player_map} and updates or merges the rest of the player data.
     * This method also handles transaction management to ensure data consistency.
     * <p>
     * This method performs the following steps:
     * 1. Reads the existing player data from the database using {@code read(DataSource.HIBERNATE)}.
     * 2. Removes any players from the database that are not present in the {@code player_map}.
     * 3. Updates or merges player information for all players in {@code player_map}.
     * 4. Commits the transaction if successful, otherwise rolls back the transaction in case of an exception.
     *
     * @param player_map a {@code TreeMap<Integer, Player>} containing the player data to be exported,
     *                   where the key is the player's ID and the value is the {@code Player} object.
     *                   This data is used to synchronize with the database.
     * @throws DatabaseException if an error occurs during the export operation. The exception contains
     *                            the cause of the failure.
     */
    private void exportHibernate(TreeMap<Integer,Player> player_map) {
        logger.info("Export Hibernate: Exporting player data...");
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            TreeMap<Integer, Player> existed_player_map = read(DataSource.HIBERNATE);
            for(Map.Entry<Integer, Player> entry : existed_player_map.entrySet()){
                if(!player_map.containsKey(entry.getKey())){
                    session.remove(entry.getValue());
                }
            }
            for(Player player : player_map.values()){
                session.merge(player);
            }
            transaction.commit();
        } catch (Exception e) {
            logger.error("Export Hibernate: Failed to export data, rollback data. Cause: {}", e.getMessage());
            if (transaction != null) {
                transaction.rollback();
                logger.info("Export Hibernate: Data is rollback");
            }
            throw new DatabaseException("Failed to exportFile via hibernate. Cause: " + e.getMessage());
        }
        logger.info("Export Hibernate: Finished!");
    }

    private void exportObjectDB(TreeMap<Integer, Player> playerMap) {
        logger.info("Export ObjectDB: Exporting player data...");
        try{
            //TreeMap<Integer, Player> existed_player_map = read(DataSource.OBJECTDB);
            entityManager.getTransaction().begin();
            /*for(Map.Entry<Integer, Player> entry : existed_player_map.entrySet()){
                if(!playerMap.containsKey(entry.getKey())){
                    entityManager.remove(entry.getValue());
                }
            }*/
            for(Player player : playerMap.values()){
                entityManager.merge(player);
            }
            entityManager.getTransaction().commit();
        }catch(Exception e){
            logger.error("Export ObjectDB: Failed to export data, rollback data. Cause: {}", e.getMessage());
            if (entityManager.getTransaction() != null) {
                entityManager.getTransaction().rollback();
            }
            throw new ObjectDBException("Failed to exportFile via hibernate. Cause: " + e.getMessage());
        }
        logger.info("Export ObjectDB: Finished!");
    }

    /**
     * Sets the database connection URL in the configuration.
     *
     * @param url the database URL to be set for the Hibernate connection.
     * It updates the "hibernate.connection.url" property in the configuration.
     */
    public void setURL(String url) {
        configuration.setProperty("hibernate.connection.url", url);
    }

    /**
     * Sets the username for the Hibernate database connection.
     * This method updates the "hibernate.connection.username" property
     * in the configuration using the provided user value.
     *
     * @param user the username to be set for the database connection
     */
    public void setUser(String user) {
        configuration.setProperty("hibernate.connection.username", user);
    }

    /**
     * Sets the database connection password in the configuration properties.
     * This method updates the "hibernate.connection.password" property with the
     * specified password.
     *
     * @param password the password to set for the database connection
     */
    public void setPassword(String password) {
        configuration.setProperty("hibernate.connection.password", password);
    }

}

