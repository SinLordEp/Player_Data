package data.database;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.DatabaseException;
import exceptions.ObjectDBException;
import exceptions.OperationException;
import model.DataInfo;
import model.Player;

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
public class ObjectDBCRUD implements GeneralCRUD<DataInfo> {
    private final DataInfo dataInfo;
    private EntityManager entityManager;

    public ObjectDBCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() throws DatabaseException {
        try{
            entityManager = Persistence.createEntityManagerFactory(dataInfo.getUrl()).createEntityManager();
            if(entityManager != null && entityManager.isOpen()){
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
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        try{
            entityManager.getTransaction().begin();
            TypedQuery<VerifiedEntity> query = switch (operation){
                case READ -> entityManager.createQuery("SELECT s FROM %s s".formatted(dataInfo.getTable()), VerifiedEntity.class);
                case SEARCH -> entityManager.createQuery("SELECT s FROM %s s WHERE s.ID = %s".formatted(dataInfo.getTable(), ((TreeMap<?, ?>) dataMap).firstKey()), VerifiedEntity.class);
                default -> throw new OperationException("Unexpected DataOperation for reading: " + operation);
            };
            ((TreeMap<?, ?>) dataMap).clear();
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
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        try {
            entityManager.getTransaction().begin();
            switch(operation){
                case ADD -> entityManager.persist(object);
                case MODIFY -> entityManager.merge(object);
                case DELETE -> entityManager.remove(entityManager.merge(object));
            }
            entityManager.getTransaction().commit();
        }catch(Exception e){
            if(entityManager.getTransaction() != null){
                entityManager.getTransaction().rollback();
            }
            throw new ObjectDBException(e.getMessage());
        }
        return this;
    }

    public GeneralCRUD<DataInfo> update(HashMap<Player, DataOperation> changed_player_map) {
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
            throw new ObjectDBException(e.getMessage());
        }
        return this;
    }

}
