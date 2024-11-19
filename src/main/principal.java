package main;

import GUI.GeneralDialog;
import Interface.GeneralControl;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.LoggerFactory;

import java.io.File;

public class principal {
    static final ClassRegister classRegister = ClassRegister.getInstance();

    public static void main(String[] args) throws Exception {
        initializeLogger();
        GeneralControl current_control = initializeControl();
        if (current_control != null) {
            current_control.run();
        }
    }

    public static void initializeLogger(){
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        context.reset();
        try {
            configurator.doConfigure(new File("src/config/logback.xml"));
        } catch (JoranException e) {
            throw new RuntimeException("Initialize logger failed", e);
        }
    }

    public static GeneralControl initializeControl(){
        try{
            String chosen_control = GeneralDialog.getDialog().selectionDialog("controller", classRegister.getControlClasses());
            return classRegister.getControl(chosen_control);
        }catch (Exception e) {
            GeneralDialog.getDialog().message(e.getMessage());
        }
        return null;
    }
}
