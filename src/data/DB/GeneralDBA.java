package data.DB;
public interface GeneralDBA<T> {
    boolean connect() throws Exception;
    T read() throws Exception;
}
