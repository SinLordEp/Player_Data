package main;

import Interface.GeneralControl;
import Interface.GeneralDataAccess;
import exceptions.OperationCancelledException;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClassRegister {
    public static Map<String, Class<? extends GeneralControl>> classMap = new HashMap<>();
    public static Map<String,Class<? extends GeneralDataAccess>> dataAccessMap = new HashMap<>();
    private static final ClassRegister instance = new ClassRegister();

    private ClassRegister() {
        registerControl();
        registerDataAccess();
    }
    public static ClassRegister getInstance() {
        return instance;
    }

    private void registerControl() {
        Reflections reflections = new Reflections("control");
        Set<Class<? extends GeneralControl>> controlSet = reflections.getSubTypesOf(GeneralControl.class);
        for (Class<? extends GeneralControl> controlClass : controlSet) {
            classMap.put(controlClass.getSimpleName().replace("Control",""), controlClass);
        }
    }

    private void registerDataAccess() {
        Reflections reflections = new Reflections("data");
        Set<Class<? extends GeneralDataAccess>> dataSet = reflections.getSubTypesOf(GeneralDataAccess.class);
        for (Class<? extends GeneralDataAccess> dataClass : dataSet) {
            dataAccessMap.put(dataClass.getSimpleName().replace("DataAccess",""), dataClass);
        }
    }

    public String[] getControlClasses() {
        return classMap.keySet().toArray(new String[0]);
    }

    public GeneralControl getControl(String class_name) throws Exception {
        Class<? extends GeneralControl> controlClass = classMap.get(class_name);
        if (controlClass != null) {
            GeneralControl control = controlClass.getDeclaredConstructor().newInstance();
            control.setDA(getDA(class_name));
            return control;
        }
        throw new OperationCancelledException("Failed to getDialog Control class");
    }
    public GeneralDataAccess getDA(String class_name) throws Exception {
        Class<? extends GeneralDataAccess> dataAccessClass = dataAccessMap.get(class_name);
        if (dataAccessClass != null) {
            return dataAccessClass.getDeclaredConstructor().newInstance();
        }
        throw new OperationCancelledException("Failed to getDialog DataAccess class");
    }
}
