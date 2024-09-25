package GUI;


import model.PlayerOperationData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static GUI.GUI_utils.buildSelectionDialog;

public class Player_menu {
    public static int extension_player() {
        String[] options = {"Binary DAT File", "XML File","TXT File"};
        return buildSelectionDialog("Choose a file type", options);
    }

    public static int file_menu(){
        String[] options = {"Create new storage file", "Read from existed file"};
        return buildSelectionDialog("Choose the file source:", options);
    }

    public static int operation_menu(String absolutePath){
        String[] options = {"Show Data", "Modify data", "Export data"};
        return buildSelectionDialog("""
                        Path: %s
                        Choose a operation""".formatted(absolutePath), options);
    }

    public static int modify_menu(){
        String[] options = {"Create new Player", "Modify Player data", "Delete Player"};
        return buildSelectionDialog("Choose a operation", options);
    }

    public static int ID_input_UI() throws NumberFormatException{
        return Integer.parseInt(JOptionPane.showInputDialog("Enter the ID of Player you wish to modify"));
    }

    public static int modify_player_menu(){
        String[] options = {"Region", "Server", "Name", "ALL"};
        return buildSelectionDialog("Choose the data you need to modify", options);
    }

    public static String name_input_UI(){
        return JOptionPane.showInputDialog("Enter player name: ");
    }

    public static String region_chooser(PlayerOperationData current_data) throws Exception {
        Set<String> regions = current_data.getRegion_server().keySet();
        List<String> regionList = new ArrayList<>(regions);
        int option = buildSelectionDialog("Choose a region: " , regionList.toArray(new String[0]));
        if(option == -1) throw new Exception("Operation canceled");
        return regionList.get(option-1);
    }

    public static String server_chooser(PlayerOperationData current_data, String region) throws Exception {
        String[] servers = current_data.getServer(region);
        int option = buildSelectionDialog("Choose a server: ", servers);
        if(option == -1) throw new Exception("Operation canceled");
        return servers[option-1];
    }

    public static void message(String msg_type){
        JOptionPane.showMessageDialog(null, switch (msg_type){
            case "Empty Map" -> "No player data registered";
            case "ID Format" -> "ID format incorrect";
            case "Modify" -> "Modification completed";
            default -> "Unhandled or unknown error";
        });
    }
}
