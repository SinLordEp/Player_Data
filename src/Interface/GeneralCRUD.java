package Interface;

import data.DataOperation;

/**
 * @author SIN
 */
public interface GeneralCRUD<T> {
    GeneralCRUD<T> prepare();
    void release();
    <R,U> GeneralCRUD<T> read(ParserCallBack<R,U> parser, DataOperation operation, U dataMap);
    <R,U> GeneralCRUD<T> update(ParserCallBack<R,U> parser, DataOperation operation, U object);
}
