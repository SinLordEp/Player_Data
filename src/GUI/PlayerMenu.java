package GUI;

import static GUI.GeneralMenu.buildSelectionDialog;

public class PlayerMenu {

    public static String run_menu(){
        String[] options = {"Create new source", "Read from existed source", "Read from DataBase"};
        return buildSelectionDialog("File Operation Menu","Choose the file source:", options);
    }

    public static String operation_menu(String path){
        String[] options = {"Show Data", "Modify data", "Export data"};
        return buildSelectionDialog("Operation Menu","""
                        Path: %s
                        Choose a operation""".formatted(path), options);
    }

    public static String modify_menu(){
        String[] options = {"Create new Player", "Modify Player data", "Delete Player"};
        return buildSelectionDialog("Modify Menu","Choose a operation", options);
    }

    public static int ID_input_UI() throws NumberFormatException{
        return Integer.parseInt(GeneralMenu.universalInput("Enter the ID"));
    }

    public static String modify_player_menu(){
        String[] options = {"Region", "Server", "Name", "ALL"};
        return buildSelectionDialog("Modifying Player","Choose the data you need to update", options);
    }


    public static String region_chooser(String[] region_list) {
        return buildSelectionDialog("Region Menu","Choose a region: " , region_list);
    }

    public static String server_chooser(String[] server_list){
        return buildSelectionDialog("Server Menu","Choose a server: ", server_list);
    }

    public static String export_menu(){
        String[] options = {"Export to file", "Export all to database (Not recommended)"};
        return buildSelectionDialog("Exporting data","Choose the export type", options);
    }
}
