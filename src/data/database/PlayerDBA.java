package data.database;

import GUI.GeneralDialog;
import Interface.GeneralDBA;
import data.DataOperation;
import model.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class PlayerDBA implements GeneralDBA<TreeMap<Integer,Player>> {
    Configuration configuration = new Configuration();
    SessionFactory sessionFactory;

    public PlayerDBA()  {
        configuration.configure("config/hibernate.cfg.xml");
    }

    @Override
    public boolean connect() {
        sessionFactory = configuration.buildSessionFactory();
        return sessionFactory.isOpen();
    }

    public boolean connected(){
        return sessionFactory.isOpen();
    }

    @Override
    public TreeMap<Integer, Player> read() {
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

    public void update(HashMap<Player,DataOperation> changed_player_map){
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

    public void update(DataOperation dataOperation, TreeMap<Integer,Player> player_map){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            if(dataOperation.equals(DataOperation.EXPORT)){
                // read database first to remove non-exist data
                TreeMap<Integer, Player> temp = read();
                for(Player player : temp.values()){
                    if(!player_map.containsKey(player.getID())){
                        session.remove(player);
                    }
                }
                for(Player player : player_map.values()){
                    session.merge(player);
                }
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

