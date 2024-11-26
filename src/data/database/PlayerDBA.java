package data.database;

import Interface.GeneralDBA;
import data.DataOperation;
import data.DataSource;
import exceptions.DatabaseException;
import model.Player;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.net.URL;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static main.principal.getProperty;

public class PlayerDBA extends GeneralDBA<TreeMap<Integer, Player>> {
    public PlayerDBA()  {
        URL resource = getClass().getResource(getProperty("hibernateConfig"));
        configuration.configure(resource);
    }

    @Override
    public boolean connect(DataSource dataSource) {
        return switch (dataSource){
            case DATABASE -> connectDatabase();
            case HIBERNATE -> connectHibernate();
            default -> throw new IllegalStateException("Unexpected value: " + dataSource);
        };
    }

    private boolean connectDatabase() {
        try {
            switch (dialect){
                case MYSQL:
                    connection = DriverManager.getConnection("%s:%s/%s".formatted(
                            login_info.get("text_url"),
                            login_info.get("text_port"),
                            login_info.get("text_database")));
                    break;
                case SQLITE:
                    connection = DriverManager.getConnection(login_info.get("text_url"));
                    break;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to connect via database. Cause: " + e.getMessage());
        }
        return connection != null;
    }

    private boolean connectHibernate() {
        switch (dialect){
            case MYSQL:
                setURL("%s:%s/%s".formatted(
                        login_info.get("text_url"),
                        login_info.get("text_port"),
                        login_info.get("text_database")));
                setUser(login_info.get("text_user"));
                setPassword(login_info.get("text_pass"));
                break;
            case SQLITE:
                setURL(login_info.get("text_url"));
                break;
        }
        try {
            sessionFactory = configuration.buildSessionFactory();
        } catch (HibernateException e) {
            throw new DatabaseException("Failed to connect via hibernate. Cause: " + e.getMessage());
        }
        return sessionFactory.isOpen();
    }

    public void disconnect(DataSource dataSource){
        switch (dataSource){
            case DATABASE:
                connection = null;
            case HIBERNATE:
                sessionFactory = null;
        }
    }

    @Override
    public TreeMap<Integer, Player> read(DataSource dataSource) {
        return switch (dataSource){
            case DATABASE -> readDatabase();
            case HIBERNATE -> readHibernate();
            default -> null;
        };
    }

    public TreeMap<Integer, Player> readDatabase() {
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
        return player_map;
    }

    public TreeMap<Integer, Player> readHibernate() {
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
        return player_map;
    }

    public void update(DataSource dataSource, HashMap<Player,DataOperation> changed_player_map) {
        switch (dataSource){
            case DATABASE -> updateDatabase(changed_player_map);
            case HIBERNATE -> updateHibernate(changed_player_map);
        }
    }

    private void updateDatabase(HashMap<Player,DataOperation> changed_player_map) {
        for(Player player : changed_player_map.keySet()) {
            switch (changed_player_map.get(player)) {
                case ADD -> addPlayer(player);
                case MODIFY -> modifyPlayer(player);
                case DELETE -> deletePlayer(player);
            }
        }
    }

    private void addPlayer(Player player) throws DatabaseException {
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
    }

    private void modifyPlayer(Player player) throws DatabaseException {
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
    }

    private void deletePlayer(Player player) throws DatabaseException {
        String query = "DELETE FROM player WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, player.getID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete player via database. Cause: " + e.getMessage());
        }
    }

    private void updateHibernate(HashMap<Player,DataOperation> changed_player_map) {
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
            if(transaction != null){
                transaction.rollback();
            }
            throw new DatabaseException("Failed to modify player via Hibernate. Cause: " + e.getMessage());
        }
    }

    public void export(DataSource dataSource, TreeMap<Integer,Player> player_map) {
        switch (dataSource){
            case DATABASE -> exportDatabase(player_map);
            case HIBERNATE -> exportHibernate(player_map);
        }
    }

    private void exportDatabase(TreeMap<Integer,Player> player_map) {
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
            throw new DatabaseException("Failed to export via database. Cause: " + e.getMessage());
        }
        disconnect(DataSource.DATABASE);
    }

    private void exportHibernate(TreeMap<Integer,Player> player_map) {
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
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DatabaseException("Failed to export via hibernate. Cause: " + e.getMessage());
        }
        disconnect(DataSource.HIBERNATE);
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

