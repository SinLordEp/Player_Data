package Interface;
public interface GeneralDBA<T> {
    boolean connect() throws Exception;
    T read() throws Exception;
}
