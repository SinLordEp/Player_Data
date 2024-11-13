package main;

import GUI.GeneralUtil;
import Interface.GeneralControl;

import static GUI.GeneralUtil.buildSelectionDialog;



public class principal {
    static final ClassRegister classRegister = ClassRegister.getInstance();
    public static void main(String[] args) throws Exception {
        GeneralControl current_control = initialize();
        current_control.run();
    }

    public static GeneralControl initialize(){
        while(true){
            try{
                String chosen_control = buildSelectionDialog("Controller menu","Choose a controller", classRegister.getControlClasses());
                GeneralControl current_control = classRegister.getControl(chosen_control);
                String chosen_dataAccess = buildSelectionDialog("Data access menu","Choose a data access", classRegister.getDataAccessClasses());
                current_control.setDA(classRegister.getDA(chosen_dataAccess));
                return current_control;
            }catch (Exception e) {
                GeneralUtil.popup(e.getMessage());
            }
        }
    }
}
