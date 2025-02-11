package Interface;

import data.DataOperation;

/**
 * @author SIN
 */
public interface PlayerCRUD<T> {
    PlayerCRUD<T> prepare(T input);
    void release();
    <R,U> PlayerCRUD<T> read(ParserCallBack<R,U> parser, DataOperation operation, U dataMap);
    <R,U> PlayerCRUD<T> update(ParserCallBack<R,U> parser, DataOperation operation, U object);
}
