package main;

import Interface.GeneralControl;
import exceptions.ConfigErrorException;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The {@code ClassRegister} class provides a Singleton implementation to dynamically register and
 * retrieve mappings of Control and DataAccess classes based on their types.
 * It searches the specified packages for subclasses of {@code GeneralControl} and {@code GeneralDataAccess},
 * maps their simple class names without "Control" or "DataAccess" suffixes, and makes them accessible.
 * @author SIN
 */
public class ControllerRegister {
    public static Map<String, Class<? extends GeneralControl>> classMap = new HashMap<>();
    private static final ControllerRegister INSTANCE = new ControllerRegister();

    /**
     * Private constructor for the {@code ClassRegister} class.
     * This constructor initializes the class by dynamically registering Control and DataAccess classes
     * to their respective mappings. It invokes {@code registerControl} to populate the map of Control classes
     * and {@code registerDataAccess} to populate the map of DataAccess classes.
     * <p>
     * This constructor is part of the Singleton implementation and is designed to be called once by the
     * static {@code INSTANCE} field to ensure that all required mappings are registered during initialization.
     */
    private ControllerRegister() {
        registerControl();
    }
    public static ControllerRegister getInstance() {
        return INSTANCE;
    }

    /**
     * Dynamically registers subclasses of {@code GeneralControl} to a mapping.
     * <p>
     * This method identifies all classes in the "control" package that extend the
     * {@code GeneralControl} interface using the Reflections library. It then maps
     * each identified class to a simplified name derived by removing the "Control"
     * suffix from the class name. The resulting mapping is used to register and manage
     * control classes dynamically within the application.
     * <p>
     * This method is invoked during the initialization of the {@code ClassRegister}
     * class, as part of the constructor. It enables the system to discover and manage
     * control classes without hardcoding them, improving flexibility and extensibility.
     * <p>
     * Calls to this method rely on the consistent naming convention where classes
     * implementing {@code GeneralControl} end with "Control". Instances are thus
     * registered in a way that allows their simple lookup and retrieval by their
     * derived name.
     */
    private void registerControl() {
        Reflections reflections = new Reflections("control");
        Set<Class<? extends GeneralControl>> controlSet = reflections.getSubTypesOf(GeneralControl.class);
        for (Class<? extends GeneralControl> controlClass : controlSet) {
            classMap.put(controlClass.getSimpleName().replace("Control",""), controlClass);
        }
    }

    /**
     * Retrieves an array of registered control class names.
     * This method extracts the keys from the internal control class mapping and returns
     * them as an array of strings. The returned class names represent the simplified keys
     * used to register and identify control classes dynamically within the application.
     * <p>
     * The method facilitates dynamic selection or retrieval of control classes, enabling
     * components like selection dialogs or initialization routines to operate without
     * hardcoding specific control class names.
     *
     * @return an array of strings containing the names of the registered control classes.
     *         The array is derived from the keys of the internal {@code classMap}.
     */
    public String[] getControlClasses() {
        return classMap.keySet().toArray(new String[0]);
    }

    /**
     * Retrieves an instance of a class implementing {@code GeneralControl} based on the provided class name.
     * The method dynamically instantiates the control class, sets its associated data access object
     * using {@code setDA}, and returns the control instance.
     *
     * @param class_name the name of the control class to be retrieved. It must match a key in the control class map.
     * @return an instance of the requested {@code GeneralControl} class, initialized with the appropriate data access object.
     * @throws Exception if the control class could not be found, instantiated, or if there is an issue with configuration.
     */
    public GeneralControl getControl(String class_name) throws Exception {
        Class<? extends GeneralControl> controlClass = classMap.get(class_name);
        if (controlClass != null) {
            return controlClass.getDeclaredConstructor().newInstance();
        }
        throw new ConfigErrorException("Failed to getDialog Control class");
    }

}
