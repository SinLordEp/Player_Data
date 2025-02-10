package Interface;

/**
 * @author SIN
 */
@FunctionalInterface
public interface CallBack<T> {
    void onSubmit(T object);
}
