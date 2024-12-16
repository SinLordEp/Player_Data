package exceptions;

/**
 * Exception thrown to represent a generic failure of an operation.
 * This exception extends {@code RuntimeException}, allowing it to be used
 * in scenarios where operations encounter unexpected runtime issues
 * that need to be explicitly identified and managed.
 * <p>
 * Use this exception when a specific operational error, unrelated to
 * other predefined categories (e.g., file, database, data type), occurs.
 * This provides a catch-all mechanism for handling unanticipated
 * operation failures in runtime workflows.
 * @author SIN
 */
public class OperationException extends RuntimeException {
    public OperationException(String message) {
        super(message);
    }
}
