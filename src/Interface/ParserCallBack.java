package Interface;

import data.DataOperation;

/**
 * @author SIN
 */
@FunctionalInterface
public interface ParserCallBack<T,R> {
    void parse(T data, DataOperation operation, R object);
}
