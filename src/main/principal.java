package main;

import GUI.Player.PlayerText;
import Interface.GeneralControl;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * The {@code principal} class serves as the entry point for the application.
 * It handles initialization processes, including loading properties, configuring
 * the logger, and setting up the main controller for the application.
 * <p>
 * The class relies on external configuration files, runtime class registration,
 * and dynamic instance management for seamless functionality. It is designed to
 * manage the system setup and invoke the main application logic.
 * <p>
 * Key Initialization Procedures:
 * - Load properties from an external configuration file using {@code initializeProperties()}.
 * - Configure and initialize the logger using {@code initializeLogger()}.
 * - Dynamically retrieve and load the main controller using {@code initializeControl()}.
 * @author SIN
 */
public class principal {
    private static final Logger logger = LoggerFactory.getLogger(principal.class);
    static final ClassRegister CLASS_REGISTER = ClassRegister.getInstance();
    private static final Properties PROPERTIES = new Properties();

    public static void main(String[] args){
        initializeProperties();
        initializeLogger();
        logger.info("Getting controller...");
        GeneralControl current_control = initializeControl();
        if (current_control != null) {
            logger.info("Controller loaded");
            current_control.run();
        }
    }

    /**
     * Loads configuration properties from the {@code /config.properties} file.
     * This method initializes the application's {@code PROPERTIES} object by reading
     * key-value pairs from the specified configuration file located in the classpath.
     * <p>
     * If the properties file is missing or cannot be loaded due to an {@code IOException},
     * the method throws a {@code RuntimeException} to halt the application's execution.
     * <p>
     * This method is a critical part of the initialization process as it provides
     * application-wide configuration values required during runtime. It must be called
     * before accessing any configuration properties using {@code getProperty(String)}.
     */
    private static void initializeProperties() {
        try{
            PROPERTIES.load(principal.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes the logging system for the application.
     * <p>
     * This method configures the logger for the application using Logback. It loads
     * the logging configuration file specified under the property {@code logbackConfig}
     * using the {@code getProperty} method. The configuration file is expected to be
     * located in the application's resources.
     * <p>
     * The method resets the current logger context to ensure any previous configuration
     * does not interfere and then applies the new configuration using {@code doConfigure}.
     * <p>
     * It is critical to call this method during the application startup to ensure
     * that logging is properly initialized before any logging statements are executed.
     * If the logging configuration file is not found or cannot be applied, the method
     * throws a {@code RuntimeException}, halting the application and signaling a failure
     * in the logger setup process.
     * <p>
     * This method calls {@code getProperty} to fetch the path to the logback configuration file.
     */
    public static void initializeLogger(){
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        try {
            URL resource = principal.class.getResource(getProperty("logbackConfig"));
            if (resource != null) {
                configurator.doConfigure(resource);
            }
        } catch (Exception e) {
            throw new RuntimeException("Initialize logger failed", e);
        }
        logger.info("Logger loaded");
    }

    /**
     * Initializes and returns a {@code GeneralControl} instance based on user selection.
     * This method prompts the user with a dialog to choose a controller from the list of
     * available control classes. The selected controller is then retrieved from
     * {@code CLASS_REGISTER} and returned as a {@code GeneralControl} instance.
     * <p>
     * If an error occurs during the dialog interaction or the retrieval process, it catches
     * the exception and displays the error message using a message dialog accessed via
     * {@code PlayerText.getDialog().message(String)}. In such cases, the method returns null.
     * <p>
     * This method handles critical initialization logic that aligns with the application's
     * architecture, particularly in loading and activating user-selected controllers.
     *
     * @return the initialized {@code GeneralControl} instance if successful, or null if an
     *         error occurs during the initialization process.
     */
    public static GeneralControl initializeControl(){
        try{
            String chosen_control = (String) PlayerText.getDialog().selectionDialog("controller", CLASS_REGISTER.getControlClasses());
            return CLASS_REGISTER.getControl(chosen_control);
        }catch (Exception e) {
            PlayerText.getDialog().message(e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the value of a specified property from the application's {@code PROPERTIES} object.
     * This method looks up the given key in the application's configuration properties and returns
     * the corresponding value.
     *
     * @param property the key of the property to retrieve
     * @return the value associated with the given key, or {@code null} if the key does not exist
     */
    public static String getProperty(String property){
        return PROPERTIES.getProperty(property);
    }
}
