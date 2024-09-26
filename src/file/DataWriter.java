package file;

public interface DataWriter<T> {
    void write(String file_path, T data) throws Exception;
}
