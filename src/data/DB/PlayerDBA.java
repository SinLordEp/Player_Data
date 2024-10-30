package data.DB;

import GUI.GeneralMenu;
import model.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;
import java.util.TreeMap;

public class PlayerDBA implements GeneralDBA<TreeMap<Integer,Player>> {
    Configuration configuration = new Configuration();
    SessionFactory sessionFactory;

    public PlayerDBA()  {
    }


    @Override
    public boolean connect() {
        sessionFactory = configuration.configure("config/hibernate.cfg.xml").buildSessionFactory();
        return sessionFactory.isOpen();
    }

    @Override
    public boolean disconnect(){
        sessionFactory.close();
        return !sessionFactory.isOpen();
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

    public void new_transaction(String operation, Player player){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            switch(operation){
                case "add": session.persist(player); break;
                case "modify": session.merge(player); break;
                case "remove": session.remove(player); break;
            }
            transaction.commit();
        }catch(Exception e){
            if(transaction != null){
                transaction.rollback();
            }
        }
    }

    public void new_transaction(String operation, TreeMap<Integer,Player> player_map){
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            switch(operation){
                case "import":
                    TreeMap<Integer, Player> temp = read();
                    for(Player player : temp.values()){
                        if(!player_map.containsKey(player.getID())){
                            session.remove(player);
                        }
                    }
                    for(Player player : player_map.values()){
                        session.merge(player);
                    }
                    break;
                case "future operation":
                    break;
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            GeneralMenu.exception_message(e);
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

