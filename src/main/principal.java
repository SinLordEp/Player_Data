package main;

import GUI.GeneralText;
import Interface.GeneralControl;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author SIN
 */
public class principal {
    static final ClassRegister CLASS_REGISTER = ClassRegister.getInstance();
    private static final Properties PROPERTIES = new Properties();

    public static void main(String[] args){
        initializeProperties();
        initializeLogger();
        GeneralControl current_control = initializeControl();
        if (current_control != null) {
            current_control.run();
        }
    }

    private static void initializeProperties() {
        try{
            PROPERTIES.load(principal.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
    }

    public static GeneralControl initializeControl(){
        try{
            String chosen_control = (String) GeneralText.getDialog().selectionDialog("controller", CLASS_REGISTER.getControlClasses());
            return CLASS_REGISTER.getControl(chosen_control);
        }catch (Exception e) {
            GeneralText.getDialog().message(e.getMessage());
        }
        return null;
    }

    public static String getProperty(String property){
        return PROPERTIES.getProperty(property);
    }
}
