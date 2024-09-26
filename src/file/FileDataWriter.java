package file;

public interface FileDataWriter<T> {
    void write(String file_path, T data) throws Exception;
}
