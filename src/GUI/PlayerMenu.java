package GUI;

import main.OperationCanceledException;

import javax.swing.*;

import static GUI.GeneralMenu.buildSelectionDialog;

public class PlayerMenu {

    public static String file_menu(){
        String[] options = {"Create new storage file", "Read from existed file"};
        return buildSelectionDialog("File Operation Menu","Choose the file source:", options);
    }

    public static String operation_menu(String absolutePath){
        String[] options = {"Show Data", "Modify data", "Export data"};
        return buildSelectionDialog("Operation Menu","""
                        Path: %s
                        Choose a operation""".formatted(absolutePath), options);
    }

    public static String modify_menu(){
        String[] options = {"Create new Player", "Modify Player data", "Delete Player"};
        return buildSelectionDialog("Modify Menu","Choose a operation", options);
    }

    public static int ID_input_UI() throws NumberFormatException{
        return Integer.parseInt(JOptionPane.showInputDialog("Enter the ID of Player you wish to modify"));
    }

    public static String modify_player_menu(){
        String[] options = {"Region", "Server", "Name", "ALL"};
        return buildSelectionDialog("Modifying Player","Choose the data you need to modify", options);
    }


    public static String region_chooser(String[] region_list) {
        return buildSelectionDialog("Region Menu","Choose a region: " , region_list);
    }

    public static String server_chooser(String[] server_list){
        return buildSelectionDialog("Server Menu","Choose a server: ", server_list);
    }

    public static void message(String msg_type){
        GeneralMenu.message_popup(switch (msg_type){
            case "Empty Map" -> "No player data registered";
            case "ID Format" -> "ID format incorrect";
            case "Modify" -> "Modification completed";
            default -> "Unhandled or unknown error";
        });
    }

    public static void exception_message(Exception e){
        if(e instanceof OperationCanceledException){
            GeneralMenu.message_popup("Operation canceled");
        }else{
            GeneralMenu.message_popup(e.getMessage());
        }
    }
}
