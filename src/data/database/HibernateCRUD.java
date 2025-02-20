package data.database;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.DatabaseException;
import model.DataInfo;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.net.URL;
import java.util.List;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class HibernateCRUD implements GeneralCRUD<DataInfo> {
    DataInfo dataInfo;
    private SessionFactory sessionFactory;
    private final Configuration configuration = new Configuration();

    /**
     * Establishes a Hibernate-based connection to a database using the provided {@code DatabaseInfo}.
     * The method determines the SQL dialect (e.g., {@code MYSQL}, {@code SQLITE}) and configures the
     * connection parameters accordingly, including database URL, username, and password. It attempts
     * to build the Hibernate session factory and verifies if the session is successfully opened.
     * Logs the connection activity and throws a {@code DatabaseException} if there is an error during
     * the connection setup.
     *
     * @param dataInfo the information required to establish the Hibernate connection,
     *                     including the SQL dialect, database URL, port, database name,
     *                     username, and password.
     * @return {@code true} if the Hibernate session is successfully opened;
     *         otherwise throws a {@code DatabaseException}.
     * @throws DatabaseException if the connection fails due to errors in configuration,
     *                           invalid credentials, or exceptions during session factory creation.
     */
    @Override
    public GeneralCRUD<DataInfo> prepare(DataInfo dataInfo) throws DatabaseException {
        URL resource = getClass().getResource(getProperty("hibernateConfig"));
        configuration.configure(resource);
        switch (dataInfo.getDialect()){
            case MYSQL:
                setURL("%s:%s/%s".formatted(
                        dataInfo.getUrl(),
                        dataInfo.getPort(),
                        dataInfo.getDatabase()));
                setUser(dataInfo.getUser());
                setPassword(dataInfo.getPassword());
                break;
            case SQLITE:
                setURL(dataInfo.getUrl());
                break;
        }
        try {
            sessionFactory = configuration.buildSessionFactory();
            if(sessionFactory != null){
                this.dataInfo = dataInfo;
                return this;
            }else{
                throw new DatabaseException("SessionFactory is null");
            }
        } catch (HibernateException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void release() {
        sessionFactory = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> search(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        try (Session session = sessionFactory.openSession()) {
            Query<VerifiedEntity> query = session.createQuery(dataInfo.getQuerySearch(), VerifiedEntity.class);
            query.setParameter("id", ((TreeMap<?, ?>) dataMap).firstKey());
            ((TreeMap<?, ?>) dataMap).clear();
            List<VerifiedEntity> list = query.getResultList();
            parser.parse((R)list, null, dataMap);
        }catch (Exception e){
            throw new DatabaseException(e.getMessage());
        }
        return this;
    }

    /**
     * Reads all player data from the database using Hibernate and returns it as a {@code TreeMap}.
     * This method utilizes a Hibernate session to execute an HQL query that retrieves {@code Player}
     * objects along with their associated {@code Region} and {@code Server} entities.
     * Each {@code Player} object is mapped by its ID in the resulting {@code TreeMap}.
     * <p>
     * The method logs the start and end of the reading process and throws a {@code DatabaseException}
     * if any issue arises during the operation. It ensures that the Hibernate session is properly
     * managed within a try-with-resources block.
     *
     * @return a {@code TreeMap<Integer, Player>} containing the player data read from the database,
     *         where the keys are player IDs, and the values are {@code Player} objects.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R,U> GeneralCRUD<DataInfo> read(ParserCallBack<R,U> parser, DataOperation operation, U dataMap){
        try (Session session = sessionFactory.openSession()) {
            List<VerifiedEntity> list = session.createQuery(dataInfo.getQueryRead(), VerifiedEntity.class).getResultList();
            parser.parse((R)list, null, dataMap);
        }catch (Exception e){
            throw new DatabaseException(e.getMessage());
        }
        return this;
    }

    @Override
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            switch(operation){
                case ADD -> session.persist(object);
                case MODIFY -> session.merge(object);
                case DELETE -> session.remove(object);
            }
            transaction.commit();
            return this;
        }catch(Exception e){
            if(transaction != null){
                transaction.rollback();
            }
            throw new DatabaseException(e.getMessage());
        }
    }

    /**
     * Sets the database connection URL in the configuration.
     *
     * @param url the database URL to be set for the Hibernate connection.
     * It updates the "hibernate.connection.url" property in the configuration.
     */
    public void setURL(String url) {
        configuration.setProperty("hibernate.connection.url", url);
    }

    /**
     * Sets the username for the Hibernate database connection.
     * This method updates the "hibernate.connection.username" property
     * in the configuration using the provided user value.
     *
     * @param user the username to be set for the database connection
     */
    public void setUser(String user) {
        configuration.setProperty("hibernate.connection.username", user);
    }

    /**
     * Sets the database connection password in the configuration properties.
     * This method updates the "hibernate.connection.password" property with the
     * specified password.
     *
     * @param password the password to set for the database connection
     */
    public void setPassword(String password) {
        configuration.setProperty("hibernate.connection.password", password);
    }
}
