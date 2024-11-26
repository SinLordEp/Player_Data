package exceptions;

/**
 * @author SIN
 */
public class DataCorruptedException extends RuntimeException {
    public DataCorruptedException(String message) {
        super(message);
    }
}
