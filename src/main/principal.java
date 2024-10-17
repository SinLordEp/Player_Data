package main;

import GUI.GeneralMenu;
import control.GeneralControl;
import control.PlayerControl;
import data.PlayerDataAccess;

import static GUI.GeneralMenu.buildSelectionDialog;



public class principal {

    public static void main(String[] args) throws Exception {
        class_register();
        GeneralControl current_control = control_chooser();
        current_control.run();
    }

    public static void class_register(){
        ClassRegister.registerControl("Player", PlayerControl.class);
        ClassRegister.registerDataAccess("Player", PlayerDataAccess.class);
        ClassRegister.registerControl("Not available", null);
        ClassRegister.registerDataAccess("Not available", null);
    }

    public static GeneralControl control_chooser(){
        while(true){
            try{
                String chosen_Class = buildSelectionDialog("Data Class Menu","Choose a data class", ClassRegister.getClassNames());
                GeneralControl current_control = ClassRegister.getControl(chosen_Class);
                current_control.setDA(ClassRegister.getDA(chosen_Class));
                return current_control;
            }catch (OperationCanceledException e){
                System.exit(0);
            }catch (Exception e) {
                GeneralMenu.message_popup(e.getMessage());
            }
        }
    }
}
