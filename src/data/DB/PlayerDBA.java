package data.DB;

import model.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;
import java.util.TreeMap;

public class PlayerDBA implements GeneralDBA<TreeMap<Integer,Player>, Player> {
    Configuration configuration = new Configuration();
    SessionFactory sessionFactory;
    Session session;

    public PlayerDBA()  {
    }


    @Override
    public boolean connect() {
        sessionFactory = configuration.configure("config/hibernate.cfg.xml").buildSessionFactory();
        session = sessionFactory.openSession();
        return session.isOpen();
    }

    @Override
    public boolean disconnect(){
        session.close();
        return !session.isOpen();
    }

    public boolean connected(){
        return session.isOpen();
    }
    @Override
    public TreeMap<Integer, Player> read() {
        String HQL = "From Player";
        Query<Player> query = session.createQuery(HQL, Player.class);
        List<Player> list = query.list();
        TreeMap<Integer, Player> player_map = new TreeMap<>();
        for(Player player : list){
            player_map.put(player.getID(), player);
        }
        return player_map;
    }

    public void importFile(TreeMap<Integer,Player> player_map) {
        TreeMap<Integer, Player> temp = read();
        for(Player player : temp.values()){
            if(!player_map.containsKey(player.getID())){
                delete(player);
            }
        }

        for(Player player : player_map.values()){
            modify(player);
        }
    }

    public void add(Player player) {
        session.persist(player);
    }

    public void modify(Player player) {
        session.merge(player);
    }

    public void delete(Player player) {
        session.remove(player);
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

