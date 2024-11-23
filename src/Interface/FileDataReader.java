package Interface;

import data.file.FileType;

public interface FileDataReader<T> {
    T read(FileType fileType, String file_path) throws Exception;
}
