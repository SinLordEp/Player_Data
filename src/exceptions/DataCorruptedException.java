package exceptions;

/**
 * Exception thrown to signal that data has been identified as corrupted or invalid.
 * This exception extends {@code RuntimeException} and can be used in scenarios
 * where data integrity issues are detected during runtime operations.
 * <p>
 * Use this exception in cases where corrupted or malformed data is encountered,
 * requiring explicit handling or propagation of error states.
 * @author SIN
 */
public class DataCorruptedException extends RuntimeException {
    public DataCorruptedException(String message) {
        super(message);
    }
}
