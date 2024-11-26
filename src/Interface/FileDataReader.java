package Interface;

import data.file.FileType;

/**
 * @author SIN
 */
public interface FileDataReader<T> {
    T read(FileType fileType, String file_path);
}
