package main;

import control.GeneralControl;
import control.PlayerControl;
import file.OperationCanceledException;

import javax.swing.*;

import static GUI.GeneralMenu.buildSelectionDialog;



public class principal {

    public static void main(String[] args) {
        initialize();
        while(true){
            try{
                String chosen_Class = buildSelectionDialog("Data Class Menu","Choose a data class",ClassRegister.getClassNames());
                GeneralControl current_control = ClassRegister.getControl(chosen_Class);
                current_control.run();
            }catch (OperationCanceledException e){
                System.exit(0);
            }catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    public static void initialize(){
        // register class
        ClassRegister.registerClass("Player", PlayerControl.class);
        ClassRegister.registerClass("Not available", GeneralControl.class);
    }

}
