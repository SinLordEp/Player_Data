package exceptions;

/**
 * Exception thrown to indicate an error in configuration settings or parameters.
 * This exception extends {@code Exception} and allows for the propagation
 * of a descriptive error message.
 * <p>
 * Typical use cases include identifying and handling issues
 * related to invalid or missing configuration values.
 * <p>
 * Use this class when configuration-related errors must be explicitly indicated
 * to distinct them from other types of runtime exceptions.
 * @author SIN
 */
public class ConfigErrorException extends Exception {
    public ConfigErrorException(String message) {
        super(message);
    }
}
