package Interface;

/**
 * @author SIN
 */
public interface FileDataWriter<T> {
    void write(String file_path, T data) throws Exception;
}
