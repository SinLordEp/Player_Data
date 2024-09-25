package main;

import model.GeneralOperationData;
import model.PlayerOperationData;

import javax.swing.*;

import static GUI.GUI_utils.buildSelectionDialog;

import static main.Player_control.player_main;


public class principal {

    public static void main(String[] args) {
        String[] options = {"Player", "Not available"};
        while(true){
            GeneralOperationData current_data;
            try{
                current_data = switch (buildSelectionDialog("Choose a data class",options)){
                    case 1 -> new PlayerOperationData();
                    case 2 -> throw new Exception("Class not available");
                    case -1 -> throw new Exception("Operation canceled");
                    default -> throw new IllegalStateException("Unrecognized class");
                };
                if(current_data instanceof PlayerOperationData){
                    player_main((PlayerOperationData) current_data);
                }else{
                    throw new IllegalStateException("Unrecognized class");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) System.exit(0);
            }
        }
    }
}
