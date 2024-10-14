package GUI.Player;

import GUI.GeneralMenu;

import static GUI.GeneralMenu.buildSelectionDialog;

public class PlayerMenu {

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
        String[] options = {"Export to data.file", "Export all to database (Not recommended)"};
        return buildSelectionDialog("Exporting data","Choose the export type", options);
    }


}
