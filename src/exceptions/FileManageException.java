package exceptions;

/**
 * Exception thrown to indicate an error in file management operations.
 * This exception extends {@code RuntimeException}, allowing it to represent
 * runtime issues specifically tied to file handling scenarios.
 * <p>
 * Use this class in operations where file-related errors must be explicitly identified
 * and propagated, such as file creation, deletion, or modification failures.
 * <p>
 * For example, this exception can be thrown within a method managing file operations
 * when a file path is invalid, permissions are insufficient, or file corruption is detected.
 * @author SIN
 */
public class FileManageException extends RuntimeException {
    public FileManageException(String message) {
        super(message);
    }
}
