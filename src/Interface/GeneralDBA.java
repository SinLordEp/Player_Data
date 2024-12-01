package Interface;

import data.DataSource;
import exceptions.DatabaseException;
import model.DatabaseInfo;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;

/**
 * @author SIN
 */
public abstract class GeneralDBA<T> {
    protected Configuration configuration = new Configuration();
    protected SessionFactory sessionFactory = null;
    protected Connection connection = null;

    protected abstract boolean connect(DatabaseInfo databaseInfo) throws DatabaseException;
    protected abstract T read(DataSource dataSource) throws Exception;
}
