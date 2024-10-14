package data.DB;

public interface GeneralDBA<T,K,V> {
    boolean connected();
    void connect() throws Exception;
    void disconnect() throws Exception;
    T read() throws Exception;
    void wipe() throws Exception;
    void add(K data) throws Exception;
    void modify(K data) throws Exception;
    void delete(V data) throws Exception;
}
