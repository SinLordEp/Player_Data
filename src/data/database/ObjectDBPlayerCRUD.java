package data.database;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.DatabaseException;
import exceptions.ObjectDBException;
import model.DataInfo;
import model.Player;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SIN
 */
public class ObjectDBPlayerCRUD implements PlayerCRUD<DataInfo> {
    private DataInfo dataInfo;
    private EntityManager entityManager;

    @Override
    public PlayerCRUD<DataInfo> prepare(DataInfo dataInfo) throws DatabaseException {
        try{
            entityManager = Persistence.createEntityManagerFactory(dataInfo.getUrl()).createEntityManager();
            if(entityManager != null && entityManager.isOpen()){
                this.dataInfo = dataInfo;
                return this;
            }else {
                throw new DatabaseException("Entity manager is closed");
            }
        }catch (Exception e){
            throw new ObjectDBException(e.getMessage());
        }
    }

    @Override
    public void release() {
        entityManager.close();
        entityManager = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        try{
            entityManager.getTransaction().begin();
            TypedQuery<VerifiedEntity> query = entityManager.createQuery("SELECT s FROM %s s".formatted(dataInfo.getTable()), VerifiedEntity.class);
            if(!query.getResultList().isEmpty()){
                List<VerifiedEntity> list = query.getResultList();
                parser.parse((R)list, operation, dataMap);
            }
            return this;
        }catch (Exception e){
            throw new ObjectDBException(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        try {
            entityManager.getTransaction().begin();
            parser.parse((R)entityManager, operation, object);
            entityManager.getTransaction().commit();
        }catch(Exception e){
            if(entityManager.getTransaction() != null){
                entityManager.getTransaction().rollback();
            }
        }
        return this;
    }

    public PlayerCRUD<DataInfo> update(HashMap<Player, DataOperation> changed_player_map) {
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
            if(entityManager.getTransaction() != null){
                entityManager.getTransaction().rollback();
            }
        }
        return this;
    }

}
