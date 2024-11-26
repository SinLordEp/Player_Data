package Interface;

/**
 * @author SIN
 */
public interface EventListener<T> {
    void onEvent(String event, T data);
}
