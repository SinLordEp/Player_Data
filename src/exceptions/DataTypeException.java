package exceptions;

/**
 * Exception thrown to indicate an error related to invalid or unsupported data types.
 * This exception extends {@code RuntimeException}, making it suitable for cases where
 * operations encounter data types that cannot be processed or are incompatible
 * with the expected structure.
 * <p>
 * Use this exception when performing validations or operations that require
 * specific data types and a mismatch is detected. For example, it is used
 * in scenarios such as data import processes to indicate an issue with the
 * type of data being handled.
 * <p>
 * This exception is explicitly caught and handled in methods like {@code importData},
 * where it logs the error message to assist in diagnosing problems.
 * @author SIN
 */
public class DataTypeException extends RuntimeException {
    public DataTypeException(String message) {
        super(message);
    }
}
