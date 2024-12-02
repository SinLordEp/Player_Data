package data.database;

import Interface.GeneralDBA;
import data.DataOperation;
import data.DataSource;
import exceptions.DatabaseException;
import model.DatabaseInfo;
import model.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class PlayerDBA extends GeneralDBA<TreeMap<Integer, Player>> {
    private static final Logger logger = LoggerFactory.getLogger(PlayerDBA.class);

    public PlayerDBA()  {
        logger.info("PlayerDBA: Instantiated");
        URL resource = getClass().getResource(getProperty("hibernateConfig"));
        configuration.configure(resource);
    }

    @Override
    public boolean connect(DatabaseInfo databaseInfo) {
        logger.info("Connect: Connecting to database via {}", databaseInfo.getUrl());
        return switch (databaseInfo.getDataSource()){
            case DATABASE -> connectDatabase(databaseInfo);
            case HIBERNATE -> connectHibernate(databaseInfo);
            default -> throw new IllegalStateException("Unexpected value: " + databaseInfo.getDataSource());
        };
    }

    private boolean connectDatabase(DatabaseInfo databaseInfo) {
        logger.info("Connect database: Connecting to database with dialect {}", databaseInfo.getDialect());
        try {
            switch (databaseInfo.getDialect()){
                case MYSQL:
                    connection = DriverManager.getConnection("%s:%s/%s".formatted(
                            databaseInfo.getUrl(),
                            databaseInfo.getPort(),
                            databaseInfo.getDatabase()));
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

    public void disconnect(DataSource dataSource){
        logger.info("Disconnect: Disconnecting from database via {}", dataSource);
        switch (dataSource){
            case DATABASE:
                connection = null;
            case HIBERNATE:
                sessionFactory = null;
        }
        logger.info("Disconnect: Success");
    }

    @Override
    public TreeMap<Integer, Player> read(DataSource dataSource) {
        logger.info("Read: Reading from database via {}", dataSource);
        TreeMap<Integer, Player> player_map = switch (dataSource){
            case DATABASE -> readDatabase();
            case HIBERNATE -> readHibernate();
            default -> null;
        };
        disconnect(dataSource);
        logger.info("Read: Finished reading from database!");
        return player_map;
    }

    public TreeMap<Integer, Player> readDatabase() {
        logger.info("Read Database: Reading data from database");
        TreeMap<Integer, Player> player_map = new TreeMap<>();
        String query = "SELECT * FROM player";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                Player player = new Player();
                player.setID(resultSet.getInt("id"));
                player.setName(resultSet.getString("name"));
                player.setRegion(resultSet.getString("region"));
                player.setServer(resultSet.getString("server"));
                player_map.put(player.getID(), player);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to read data via database. Cause: "+e.getMessage());
        }
        logger.info("Read Database: Finished reading from database!");
        return player_map;
    }

    public TreeMap<Integer, Player> readHibernate() {
        logger.info("Read Hibernate: Reading data from database");
        TreeMap<Integer, Player> player_map = new TreeMap<>();
        try (Session session = sessionFactory.openSession()) {
            String HQL = "From Player";
            Query<Player> query = session.createQuery(HQL, Player.class);
            List<Player> list = query.list();
            for(Player player : list){
                player_map.put(player.getID(), player);
            }
        }catch (Exception e){
            throw new DatabaseException("Failed to read data via hibernate. Cause: " + e.getMessage());
        }
        logger.info("Read Hibernate: Finished reading from database!");
        return player_map;
    }

    public void update(DataSource dataSource, HashMap<Player,DataOperation> changed_player_map) {
        logger.info("Update: Updating database data with changed player map...");
        switch (dataSource){
            case DATABASE -> updateDatabase(changed_player_map);
            case HIBERNATE -> updateHibernate(changed_player_map);
        }
        logger.info("Update: Finished updating database!");
    }

    private void updateDatabase(HashMap<Player,DataOperation> changed_player_map) {
        logger.info("Update Database: Updating database...");
        for(Player player : changed_player_map.keySet()) {
            switch (changed_player_map.get(player)) {
                case ADD -> addPlayer(player);
                case MODIFY -> modifyPlayer(player);
                case DELETE -> deletePlayer(player);
            }
        }
        logger.info("Update Database: Finished!");
    }

    private void addPlayer(Player player) throws DatabaseException {
        logger.info("Add Player: Adding player with ID: {}", player.getID());
        String query = "INSERT INTO player (id, region, server, name) VALUES (?,?,?,?)";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, player.getID());
            statement.setString(2, player.getRegion());
            statement.setString(3, player.getServer());
            statement.setString(4, player.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to add player via database. Cause: " + e.getMessage());
        }
        logger.info("Add Player: Finished adding player!");
    }

    private void modifyPlayer(Player player) throws DatabaseException {
        logger.info("Modify Player: Modifying player with ID: {}", player.getID());
        String query = "UPDATE player SET region = ?, server = ?, name = ? WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, player.getRegion());
            statement.setString(2, player.getServer());
            statement.setString(3, player.getName());
            statement.setInt(4, player.getID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to modify player via database. Cause: " + e.getMessage());
        }
        logger.info("Modify Player: Finished Modifying player!");
    }

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

    private void updateHibernate(HashMap<Player,DataOperation> changed_player_map) {
        logger.info("Update Hibernate: Updating database...");
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            for(Player player : changed_player_map.keySet()){
                switch(changed_player_map.get(player)){
                    case DataOperation.ADD: session.persist(player); break;
                    case DataOperation.MODIFY: session.merge(player); break;
                    case DataOperation.DELETE: session.remove(player); break;
                }
            }
            transaction.commit();
            changed_player_map.clear();
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

    public void export(DataSource dataSource, TreeMap<Integer,Player> player_map) {
        logger.info("Export: Exporting player data via {}", dataSource);
        switch (dataSource){
            case DATABASE -> exportDatabase(player_map);
            case HIBERNATE -> exportHibernate(player_map);
        }
        disconnect(dataSource);
        logger.info("Export: Finished exporting player data!");
    }

    private void exportDatabase(TreeMap<Integer,Player> player_map) {
        logger.info("Export Database: Exporting player data...");
        TreeMap<Integer, Player> target_player_map = read(DataSource.DATABASE);
        //delete non-exist ID from database
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

    private void exportHibernate(TreeMap<Integer,Player> player_map) {
        logger.info("Export Hibernate: Exporting player data...");
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            // delete non-exist ID from database
            TreeMap<Integer, Player> temp = read(DataSource.HIBERNATE);
            for(Player player : temp.values()){
                if(!player_map.containsKey(player.getID())){
                    session.remove(player);
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

    public void setURL(String url) {
        configuration.setProperty("hibernate.connection.url", url);
    }

    public void setUser(String user) {
        configuration.setProperty("hibernate.connection.username", user);
    }

    public void setPassword(String password) {
        configuration.setProperty("hibernate.connection.password", password);
    }

}

