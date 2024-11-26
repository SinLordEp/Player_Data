package Interface;

import data.DataSource;
import data.database.SqlDialect;
import exceptions.DatabaseException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.HashMap;

/**
 * @author SIN
 */
public abstract class GeneralDBA<T> {
    protected Configuration configuration = new Configuration();
    protected SessionFactory sessionFactory = null;
    protected Connection connection = null;
    protected HashMap<String,String> login_info = null;
    protected SqlDialect dialect = SqlDialect.NONE;

    protected abstract boolean connect(DataSource dataSource) throws DatabaseException;
    protected abstract T read(DataSource dataSource) throws Exception;
    public SqlDialect getDialect() {return this.dialect;}
    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }
    public void setLogin_info(HashMap<String,String> login_info) {this.login_info = login_info;}
}
