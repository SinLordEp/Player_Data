package Interface;

import GUI.LogStage;

/**
 * @author SIN
 */
public interface EventListener<T> {
    void onEvent(String event, T data);
    void onLog(LogStage logStage, String... message);
}
