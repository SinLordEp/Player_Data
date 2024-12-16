package exceptions;

/**
 * Custom exception used to represent errors originating from PHP-based HTTP responses.
 * This class extends {@code RuntimeException}, providing unchecked exception handling for
 * scenarios where PHP server-side issues need to be explicitly managed during runtime.
 * <p>
 * Use this exception in cases where the HTTP response from the PHP server indicates
 * an error condition, such as invalid response data or server-specific errors.
 * Common scenarios include interpreting JSON responses where the status indicates a failure
 * or when an exception needs to provide meaningful information about the nature of the PHP server error.
 * <p>
 * For example, this exception is thrown in {@code read_json()} methods when the status in
 * the JSON response is detected as "error". The thrown exception includes a message
 * extracted from the response, highlighting the cause of the server-side issue.
 * @author SIN
 */
public class HttpPhpException extends RuntimeException {
    public HttpPhpException(String message) {
        super(message);
    }
}
