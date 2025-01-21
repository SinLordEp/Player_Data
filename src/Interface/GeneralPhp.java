package Interface;

import data.http.PhpType;

/**
 * @author SIN
 */
public interface GeneralPhp<T> {
    T read(PhpType phpType);
    void export(PhpType phpType, T data);
}
