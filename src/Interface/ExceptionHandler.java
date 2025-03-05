package Interface;

import java.util.TreeMap;

/**
 * @author SIN
 */
public interface ExceptionHandler {

    @FunctionalInterface
    interface ExceptionWithReturn<T> {
        T run() throws Exception;
    }
    @FunctionalInterface
    interface ExceptionWithoutReturn {
        void run() throws Exception;
    }

    <T> T handle(ExceptionWithReturn<T> function, String className, String... textSubType);
    void handle(ExceptionWithoutReturn function, String className, String... textSubType);
    void addListener(EventListener<TreeMap<Integer, VerifiedEntity>> listener);
}
