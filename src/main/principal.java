package main;

import GUI.GeneralDialog;
import Interface.GeneralControl;

public class principal {
    static final ClassRegister classRegister = ClassRegister.getInstance();
    public static void main(String[] args) throws Exception {
        GeneralControl current_control = initialize();
        current_control.run();
    }

    public static GeneralControl initialize(){
        while(true){
            try{
                String chosen_control = GeneralDialog.get().selectionDialog("controller", classRegister.getControlClasses());
                return classRegister.getControl(chosen_control);
            }catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        }
    }
}
