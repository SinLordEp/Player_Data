package data.DB;
public interface GeneralDBA<T> {
    boolean connect() throws Exception;
    boolean disconnect() throws Exception;
    T read() throws Exception;
}
