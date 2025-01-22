package data.database;

import Interface.PlayerCRUD;
import data.DataOperation;
import exceptions.DatabaseException;
import model.DatabaseInfo;
import model.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class HibernatePlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private SessionFactory sessionFactory;
    private final Configuration configuration = new Configuration();

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
    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) throws DatabaseException {
        logger.info("Connect hibernate: Fetching hibernate configuration");
        URL resource = getClass().getResource(getProperty("hibernateConfig"));
        configuration.configure(resource);
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
            logger.info("Connect hibernate: Success");
            if(sessionFactory != null){
                return this;
            }else{
                throw new DatabaseException("SessionFactory is null");
            }
        } catch (HibernateException e) {
            throw new DatabaseException("Failed to connect via hibernate. Cause: " + e.getMessage());
        }
    }

    @Override
    public void release() {
        sessionFactory = null;
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
    @Override
    public TreeMap<Integer, Player> read() throws Exception {
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
        release();
        return player_map;
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
    @Override
    public void update(HashMap<Player, DataOperation> changed_player_map) {
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
        release();
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
    @Override
    public void export(TreeMap<Integer, Player> player_map) {
        logger.info("Export Hibernate: Exporting player data...");
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            TreeMap<Integer, Player> existed_player_map = read();
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
        release();
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
