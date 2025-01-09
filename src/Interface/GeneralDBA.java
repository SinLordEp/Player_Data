package Interface;

import data.DataSource;
import exceptions.DatabaseException;
import model.DatabaseInfo;

/**
 * @author SIN
 */
public interface  GeneralDBA<T> {
    boolean connect(DatabaseInfo databaseInfo) throws DatabaseException;
    T read(DataSource dataSource) throws Exception;
}
