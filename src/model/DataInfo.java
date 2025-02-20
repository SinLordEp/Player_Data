package model;

import data.database.SqlDialect;

/**
 * @author SIN
 */
public class DataInfo {
    private Object dataType;
    private SqlDialect dialect;
    private String url, port, database, user, password, table, queryRead, queryADD, queryModify, queryDelete, querySearch;

    public DataInfo() {
    }

    public DataInfo(Object dataType) {
        this.dataType = dataType;
    }

    public Object getDataType() {
        return dataType;
    }

    public void setDataType(Object dataType) {
        this.dataType = dataType;
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

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
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

    public String getQueryModify() {
        return queryModify;
    }

    public void setQueryModify(String queryModify) {
        this.queryModify = queryModify;
    }

    public String getQueryDelete() {
        return queryDelete;
    }

    public void setQueryDelete(String queryDelete) {
        this.queryDelete = queryDelete;
    }

    public String getQuerySearch() {
        return querySearch;
    }

    public void setQuerySearch(String querySearch) {
        this.querySearch = querySearch;
    }
}
