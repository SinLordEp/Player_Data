package exceptions;

/**
 * Exception thrown to indicate that an operation has been explicitly cancelled.
 * This exception extends {@code RuntimeException}, offering a mechanism to abort
 * the normal flow of execution when an operation is stopped intentionally.
 * <p>
 * Use this exception in scenarios where operation cancellation needs to be
 * explicitly represented, ensuring higher-level components can handle or
 * propagate the cancellation status appropriately.
 * <p>
 * This may be used in callback methods or asynchronous operations to
 * interrupt ongoing tasks without signaling an operational error.
 * @author SIN
 */
public class OperationCancelledException extends RuntimeException{

    public OperationCancelledException() {
        super();
    }
}
