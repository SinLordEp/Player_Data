package data.database;

import GUI.GeneralDialog;
import Interface.GeneralDBA;
import data.DataOperation;
import data.DataSource;
import model.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class PlayerDBA extends GeneralDBA<TreeMap<Integer, Player>> {
    public PlayerDBA()  {
        configuration.configure("config/hibernate.cfg.xml");
    }

    @Override
    public HashMap<String, String> getDefaultDatabaseInfo() {
        login_info = new HashMap<>();
        switch (dialect) {
            case MYSQL:
                login_info.put("text_url","jdbc:mysql://localhost");
                login_info.put("text_port","3306");
                login_info.put("text_database","person");
                login_info.put("text_user","root");
                login_info.put("text_pass","root");
                break;
            case SQLITE:
                login_info.put("text_url","jdbc:sqlite:person.db");
                break;
        }
        return null;
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
                try{
                    connection.close();
                    return connection.isClosed();
                } catch (SQLException e) {
                    GeneralDialog.getDialog().message("Failed to disconnect from database\n"+e.getMessage());
                }
            case HIBERNATE:
                sessionFactory.close();
                sessionFactory = null;
                return true;
        }
        return false;
    }

    @Override
    public TreeMap<Integer, Player> read(DataSource dataSource) {
        return switch (dataSource){
            case DATABASE -> readDatabase();
            case HIBERNATE -> readHibernate();
            default -> null;
        };
    }
    //todo
    public TreeMap<Integer, Player> readDatabase(){
        return null;
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

    //todo
    private void updateDatabase(HashMap<Player,DataOperation> changed_player_map){

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
            case DATABASE -> exportDatabase(dataSource, player_map);
            case HIBERNATE -> exportHibernate(dataSource, player_map);
        }
    }
    //todo
    private void exportDatabase(DataSource dataSource, TreeMap<Integer,Player> player_map){

    }

    private void exportHibernate(DataSource dataSource, TreeMap<Integer,Player> player_map){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            // read database first to remove non-exist data
            TreeMap<Integer, Player> temp = read(dataSource);
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

