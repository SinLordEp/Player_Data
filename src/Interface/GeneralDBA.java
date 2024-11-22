package Interface;

import data.DataSource;
import data.database.SqlDialect;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Connection;
import java.util.HashMap;

public abstract class GeneralDBA<T> {
    protected Configuration configuration = new Configuration();
    protected SessionFactory sessionFactory = null;
    protected Connection connection = null;
    protected HashMap<String,String> login_info = null;
    protected SqlDialect dialect = SqlDialect.NONE;

    protected abstract boolean connect(DataSource dataSource);
    protected abstract T read(DataSource dataSource) throws Exception;
    public abstract HashMap<String, String> getDefaultDatabaseInfo();
    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }
}
