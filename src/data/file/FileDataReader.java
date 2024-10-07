package data.file;

public interface FileDataReader<T> {
    T read(String file_path) throws Exception;
}
