package main;

import GUI.GeneralDialog;
import Interface.GeneralControl;

public class principal {
    static final ClassRegister classRegister = ClassRegister.getInstance();
    public static void main(String[] args) throws Exception {
        GeneralControl current_control = initialize();
        if (current_control != null) {
            current_control.run();
        }
    }

    public static GeneralControl initialize(){
        try{
            String chosen_control = GeneralDialog.getDialog().selectionDialog("controller", classRegister.getControlClasses());
            return classRegister.getControl(chosen_control);
        }catch (Exception e) {
            GeneralDialog.getDialog().message(e.getMessage());
        }
        return null;
    }
}
