package data.database;

import GUI.GeneralDialog;
import Interface.GeneralDBA;
import data.DataOperation;
import data.DataSource;
import main.OperationException;
import model.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
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
    @SuppressWarnings("unchecked")
    public HashMap<String, String> getDefaultDatabaseInfo() {
        login_info = new HashMap<>();
        HashMap<String,Object> default_info = null;
        URL resource = getClass().getResource(getProperty("defaultPlayerSQL"));
        if (resource != null) {
            try(InputStream inputStream = resource.openStream()){
                Yaml yaml = new Yaml();
                default_info = yaml.load(inputStream);
            }catch (Exception e){
                throw new OperationException("Error loading default database info");
            }
        }
        if(default_info == null){
            throw new OperationException("Default database info is null");
        }
        switch (dialect) {
            case MYSQL:
                HashMap<String,Object> mysql_info = (HashMap<String, Object>) default_info.get("MYSQL");
                login_info.put("text_url", (String) mysql_info.get("text_url"));
                login_info.put("text_port",(String) mysql_info.get("text_port"));
                login_info.put("text_database",(String) mysql_info.get("text_database"));
                login_info.put("text_user",(String) mysql_info.get("text_user"));
                login_info.put("text_pwd",(String) mysql_info.get("text_pwd"));
                break;
            case SQLITE:
                HashMap<String,Object> sqlite_info = (HashMap<String, Object>) default_info.get("SQLITE");
                login_info.put("text_url",(String) sqlite_info.get("text_url"));
                break;
        }
        return login_info;
    }

    @Override
    public boolean connect(DataSource dataSource) {
        return switch (dataSource){
            case DATABASE -> connectDatabase();
            case HIBERNATE -> connectHibernate();
            default -> throw new IllegalStateException("Unexpected value: " + dataSource);
        };
    }

    private boolean connectDatabase(){
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
            GeneralDialog.getDialog().message("Failed to connect to database\n"+e.getMessage());
        }
        return connection != null;
    }

    private boolean connectHibernate(){
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
        sessionFactory = configuration.buildSessionFactory();
        return sessionFactory.isOpen();
    }

    public boolean isConnected(){
        return switch (dialect){
            case MYSQL -> connection != null;
            case SQLITE -> sessionFactory != null;
            default -> false;
        };
    }

    public boolean disconnect(DataSource dataSource){
        switch (dataSource){
            case DATABASE:
                connection = null;
            case HIBERNATE:
                sessionFactory = null;
        }
        return true;
    }

    @Override
    public TreeMap<Integer, Player> read(DataSource dataSource) {
        return switch (dataSource){
            case DATABASE -> readDatabase();
            case HIBERNATE -> readHibernate();
            default -> null;
        };
    }

    public TreeMap<Integer, Player> readDatabase(){
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
            throw new OperationException("Error reading data from database");
        }
        return player_map;
    }

    public TreeMap<Integer, Player> readHibernate(){
        TreeMap<Integer, Player> player_map = new TreeMap<>();
        try (Session session = sessionFactory.openSession()) {
            String HQL = "From Player";
            Query<Player> query = session.createQuery(HQL, Player.class);
            List<Player> list = query.list();
            for(Player player : list){
                player_map.put(player.getID(), player);
            }
        }
        return player_map;
    }

    public void update(DataSource dataSource, HashMap<Player,DataOperation> changed_player_map){
        switch (dataSource){
            case DATABASE -> updateDatabase(changed_player_map);
            case HIBERNATE -> updateHibernate(changed_player_map);
        }
    }

    private void updateDatabase(HashMap<Player,DataOperation> changed_player_map){
        for(Player player : changed_player_map.keySet()) {
            switch (changed_player_map.get(player)) {
                case ADD -> addPlayer(player);
                case MODIFY -> modifyPlayer(player);
                case DELETE -> deletePlayer(player);
            }
        }
    }

    private void addPlayer(Player player){
        String query = "INSERT INTO player (id, region, server, name) VALUES (?,?,?,?)";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, player.getID());
            statement.setString(2, player.getRegion());
            statement.setString(3, player.getServer());
            statement.setString(4, player.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new OperationException("Error adding player to database");
        }
    }

    private void modifyPlayer(Player player){
        String query = "UPDATE player SET region = ?, server = ?, name = ? WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, player.getRegion());
            statement.setString(2, player.getServer());
            statement.setString(3, player.getName());
            statement.setInt(4, player.getID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new OperationException("Error modifying player in database");
        }
    }

    private void deletePlayer(Player player){
        String query = "DELETE FROM player WHERE id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, player.getID());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new OperationException("Error deleting player from database");
        }
    }

    private void updateHibernate(HashMap<Player,DataOperation> changed_player_map){
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
        }
    }


    public void export(DataSource dataSource, TreeMap<Integer,Player> player_map){
        switch (dataSource){
            case DATABASE -> exportDatabase(player_map);
            case HIBERNATE -> exportHibernate(player_map);
        }
    }

    private void exportDatabase(TreeMap<Integer,Player> player_map){
        TreeMap<Integer, Player> target_player_map = read(DataSource.DATABASE);
        //delete non-exist ID from database
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
        disconnect(DataSource.DATABASE);
    }

    private void exportHibernate(TreeMap<Integer,Player> player_map){
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
            GeneralDialog.getDialog().message("Updating database failed\n" + e.getMessage());
        }
        disconnect(DataSource.HIBERNATE);
    }


    public void setURL(String URL) {
        configuration.setProperty("hibernate.connection.url", URL);
    }

    public void setUser(String user) {
        configuration.setProperty("hibernate.connection.username", user);
    }

    public void setPassword(String password) {
        configuration.setProperty("hibernate.connection.password", password);
    }

}

