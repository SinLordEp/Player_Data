package data.database;

import Interface.PlayerCRUD;
import data.DataOperation;
import exceptions.DatabaseException;
import exceptions.ObjectDBException;
import model.DatabaseInfo;
import model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class ObjectDBPlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private EntityManager entityManager;

    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) throws DatabaseException {
        logger.info("Connecting to ObjectDB file server");
        try{
            entityManager = Persistence.createEntityManagerFactory(databaseInfo.getUrl()).createEntityManager();
            if(entityManager != null && entityManager.isOpen()){
                return this;
            }else {
                throw new DatabaseException("Entity manager is closed");
            }
        }catch (Exception e){
            throw new ObjectDBException("Failed to connect via ObjectDB. Cause: " + e.getMessage());
        }
    }

    @Override
    public void release() {
        entityManager.close();
        entityManager = null;
    }

    @Override
    public PlayerCRUD<DatabaseInfo> read(TreeMap<Integer, Player> player_map) {
        logger.info("Reading data from ObjectDB file");
        try{
            entityManager.getTransaction().begin();
            TypedQuery<Player> query = entityManager.createQuery("SELECT s FROM Player s", Player.class);
            if(!query.getResultList().isEmpty()){
                List<Player> playerList = query.getResultList();
                for(Player player : playerList){
                    player_map.put(player.getID(), player);
                }
            }
            return this;
        }catch (Exception e){
            throw new ObjectDBException("Failed to read data via ObjectDB. Cause: "+e.getMessage());
        }
    }

    @Override
    public PlayerCRUD<DatabaseInfo> update(HashMap<Player, DataOperation> changed_player_map) {
        logger.info("Updating database...");
        try {
            entityManager.getTransaction().begin();
            for(Map.Entry<Player, DataOperation> player_operation : changed_player_map.entrySet()) {
                switch (player_operation.getValue()) {
                    case ADD -> entityManager.persist(player_operation.getKey());
                    case MODIFY -> entityManager.merge(player_operation.getKey());
                    case DELETE -> entityManager.remove(player_operation.getKey());
                }
            }
            entityManager.getTransaction().commit();
        }catch(Exception e){
            logger.error("Failed to update database, rollback data. Cause: {}", e.getMessage());
            if(entityManager.getTransaction() != null){
                entityManager.getTransaction().rollback();
            }
        }
        return this;
    }

    @Override
    public PlayerCRUD<DatabaseInfo> export(TreeMap<Integer, Player> player_map) {
        logger.info("Exporting player data...");
        try{
            TreeMap<Integer, Player> existed_player_map = new TreeMap<>();
            read(existed_player_map);
            for(Map.Entry<Integer, Player> entry : existed_player_map.entrySet()){
                if(!player_map.containsKey(entry.getKey())){
                    entityManager.remove(entry.getValue());
                }
            }
            for(Player player : player_map.values()){
                entityManager.merge(player);
            }
            entityManager.getTransaction().commit();
        }catch(Exception e){
            logger.error("Failed to export data, rollback data. Cause: {}", e.getMessage());
            if (entityManager.getTransaction() != null) {
                entityManager.getTransaction().rollback();
            }
            throw new ObjectDBException("Failed to export data via ObjectDB. Cause: " + e.getMessage());
        }
        return this;
    }

}
