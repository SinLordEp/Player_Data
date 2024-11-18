package Interface;
public interface GeneralDBA<T> {
    void connect() throws Exception;
    T read() throws Exception;
}
