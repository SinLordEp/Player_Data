package exceptions;

/**
 * Exception thrown to indicate an error specific to database-related operations.
 * This exception extends {@code RuntimeException}, enabling it to be used
 * in cases where database runtime exceptions occur.
 * <p>
 * Use this exception when database operation failures or irregularities must be
 * explicitly indicated, such as connection issues, query execution failures, or
 * database integrity problems.
 * @author SIN
 */

public class DatabaseException extends RuntimeException {
    public DatabaseException(String message) {
        super(message);
    }
}
