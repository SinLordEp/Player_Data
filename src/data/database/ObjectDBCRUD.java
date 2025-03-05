package data.database;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.DataInfo;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.TreeMap;

/**
 * @author SIN
 */
@SuppressWarnings("unused")
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
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void release() {
        entityManager.close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try{
            entityManager.getTransaction().begin();
            TypedQuery<VerifiedEntity> query = switch (dataOperation){
                case READ -> entityManager.createQuery("SELECT s FROM %s s".formatted(dataInfo.getTable()), VerifiedEntity.class);
                case SEARCH -> entityManager.createQuery("SELECT s FROM %s s WHERE s.ID = %s".formatted(dataInfo.getTable(), ((TreeMap<?, ?>) dataContainer).firstKey()), VerifiedEntity.class);
                default -> throw new OperationException("Unexpected DataOperation for reading: " + dataOperation);
            };
            if(!query.getResultList().isEmpty()){
                List<VerifiedEntity> list = query.getResultList();
                parser.parse((R)list, dataOperation, dataContainer);
            }
        }catch (Exception e){
            throw new DatabaseException(e.getMessage());
        }
        return this;
    }

    @Override
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try {
            entityManager.getTransaction().begin();
            switch(dataOperation){
                case ADD -> entityManager.persist(dataContainer);
                case MODIFY -> entityManager.merge(dataContainer);
                case DELETE -> entityManager.remove(entityManager.merge(dataContainer));
            }
            entityManager.getTransaction().commit();
        }catch(Exception e){
            if(entityManager.getTransaction() != null){
                entityManager.getTransaction().rollback();
            }
            throw new DatabaseException(e.getMessage());
        }
        return this;
    }

}
