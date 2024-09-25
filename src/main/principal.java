package main;

import model.GeneralOperationData;
import model.PlayerOperationData;

import javax.swing.*;

import static GUI.GUI_utils.buildSelectionDialog;
import static GUI.Player_menu.main_menu;


public class principal {

    public static void main(String[] args) {
        String[] options = {"Player", "Not available"};
        while(true){
            GeneralOperationData current_data;
            try{
                current_data = switch (buildSelectionDialog("Choose a data class",options)){
                    case 1 -> new PlayerOperationData();
                    case 2 -> throw new Exception("Selected class is not available");
                    case -1 -> throw new Exception("Operation canceled");
                    default -> throw new IllegalStateException("Unrecognized class");
                };
                if(current_data instanceof PlayerOperationData){
                    main_menu((PlayerOperationData) current_data);
                }else{
                    throw new Exception("Class is not available");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) System.exit(0);
            }
        }
    }
}
