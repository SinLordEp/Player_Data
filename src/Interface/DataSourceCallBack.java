package Interface;

/**
 * @author SIN
 */
@FunctionalInterface
public interface DataSourceCallBack<T, R> {
    void onSubmit(T datasource, R datatype);
}
