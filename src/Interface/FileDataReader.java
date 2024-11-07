package Interface;

public interface FileDataReader<T> {
    T read(String file_path) throws Exception;
}
