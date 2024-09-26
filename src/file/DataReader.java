package file;

public interface DataReader<T> {
    T read(String file_path) throws Exception;
}
