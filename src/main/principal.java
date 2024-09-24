package main;

import model.GeneralOperationData;
import model.PersonOperationData;

import javax.swing.*;

import static GUI.GUI_utils.buildSelectionDialog;
import static GUI.Person_menu.person_menu;


public class principal {

    public static void main(String[] args) {
        GeneralOperationData current_data = null;
        String[] options = {"Person", "Not available"};
        while(current_data == null){
            try{
                current_data = switch (buildSelectionDialog("Choose a data class",options)){
                    case 1 -> new PersonOperationData();
                    case 2 -> throw new Exception("Selected class is not available");
                    case -1 -> throw new Exception("Operation canceled");
                    default ->
                            throw new IllegalStateException("Unrecognized class");
                };
                switch (current_data.getData_class()){
                    case "Person": person_menu((PersonOperationData) current_data);
                    case "Not defined": break;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) System.exit(0);
            }
        }
    }
}
