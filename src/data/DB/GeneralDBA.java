package data.DB;

public interface GeneralDBA<T,K> {
    boolean connect() throws Exception;
    boolean disconnect() throws Exception;
    T read() throws Exception;
    void importFile(T data) throws Exception;
    void add(K data) throws Exception;
    void modify(K data) throws Exception;
    void delete(K data) throws Exception;
}
