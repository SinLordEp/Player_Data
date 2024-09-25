package main;

import control.GeneralControl;

import java.util.HashMap;
import java.util.Map;

public class ClassRegister {
    public static Map<String, Class<? extends GeneralControl>> classMap = new HashMap<>();

    public static void registerClass(String className, Class<? extends GeneralControl> control_Class) {
        classMap.put(className, control_Class);
    }

    public static String[] getClassNames() {
        return classMap.keySet().toArray(new String[0]);
    }

    public static GeneralControl getControl(String class_name) throws Exception {
        Class<? extends GeneralControl> controlClass = classMap.get(class_name);
        if (controlClass != null) {
            return controlClass.getDeclaredConstructor().newInstance();
        } else {
            throw new Exception("Class not found");
        }
    }
}
