package model;

import data.DataSource;
import data.database.SqlDialect;

/**
 * @author SIN
 */
public class DatabaseInfo {
    private DataSource dataSource;
    private SqlDialect dialect;
    private String url, port, database, user, password, queryRead, queryADD, queryUpdate, queryDelete, queryExport;

    public DatabaseInfo() {
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public SqlDialect getDialect() {
        return dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQueryRead() {
        return queryRead;
    }

    public void setQueryRead(String queryRead) {
        this.queryRead = queryRead;
    }

    public String getQueryADD() {
        return queryADD;
    }

    public void setQueryADD(String queryADD) {
        this.queryADD = queryADD;
    }

    public String getQueryUpdate() {
        return queryUpdate;
    }

    public void setQueryUpdate(String queryUpdate) {
        this.queryUpdate = queryUpdate;
    }

    public String getQueryDelete() {
        return queryDelete;
    }

    public void setQueryDelete(String queryDelete) {
        this.queryDelete = queryDelete;
    }

    public String getQueryExport() {
        return queryExport;
    }

    public void setQueryExport(String queryExport) {
        this.queryExport = queryExport;
    }
}
