package Interface;

import data.http.DataType;

/**
 * @author SIN
 */
public interface GeneralPhp<T> {
    T read(DataType dataType);
    void export(DataType dataType, T data);
}
