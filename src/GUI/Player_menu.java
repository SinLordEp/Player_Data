package GUI;

import model.PlayerOperationData;
import model.Player;
import utils.*;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static GUI.GUI_utils.buildSelectionDialog;


public class Player_menu {

    public static void main_menu(PlayerOperationData current_data){
        String[] options = {"Dat File", "XML File"};
        while(current_data.getFile() == null){
            try{
                //Set extension
                current_data.setFile_extension(switch (buildSelectionDialog("Choose a data source", options)) {
                    case 1 -> "dat";
                    case 2 -> "xml";
                    case -1 -> throw new Exception("Operation canceled");
                    default -> "";
                });
                // Choose file
                JOptionPane.showMessageDialog(null,"Choosing source file");
                current_data.setFile(File_Chooser.get_path(current_data.getFile_extension()));
                if(current_data.getFile() == null) throw new Exception("Operation canceled");
                //Read file data and save to PlayerOperationData
                DataReaderFactory.initialReader(current_data);
                DataWriterFactory.initializeWriter(current_data);
                FileManager fileManager = new FileManager();
                fileManager.readData(current_data);

                operation_menu(current_data, fileManager);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    private static void operation_menu(PlayerOperationData current_data, FileManager fileManager){
        String[] options = {"Show Data", "Modify data", "Convert xml to dat File"};
        while (true){
            try{
                switch(buildSelectionDialog("""
                        Path: %s
                        Choose a operation""".formatted(current_data.getAbsolutePath()), options)){
                    case 1: current_data.print_person(); break;
                    case 2: modify_menu(current_data, fileManager); break;
                    case 3: xmlTo_dat(); break;
                    case -1: throw new Exception("Operation canceled");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    private static void modify_menu(PlayerOperationData current_data, FileManager fileManager){
        String[] options = {"Create new Player", "Modify Player data"};
        while(true){
            try {
                switch(buildSelectionDialog("Choose a operation", options)){
                    case 1: create_player(current_data); break;
                    case 2: modify_player(current_data); break;
                    case -1: throw new Exception("Operation canceled");
                }
                if(current_data.isFile_changed()) {
                    fileManager.writeData(current_data);
                    current_data.setFile_changed(false);
                    return;
                }
            }catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    private static void modify_player(PlayerOperationData current_data) throws Exception {
        String[] options = {"Region", "Server", "Name", "ALL"};
        int ID = Integer.parseInt(JOptionPane.showInputDialog("Enter the ID to modify"));
        if(!current_data.containsKey(ID)){
            JOptionPane.showMessageDialog(null,"Player does not exist");
            return;
        }
        Player player = current_data.getFrom_Map(ID);
        switch(buildSelectionDialog("Choose the data you need to modify", options)){
            // case 1 is linked to case 2, because after changing region the server has to be changed too.
            case 1: player.setRegion(region_chooser(current_data));
            case 2: player.setServer(server_chooser(current_data, player.getRegion())); break;
            case 3: player.setName(JOptionPane.showInputDialog("Enter player name: ")); break;
            case 4: player.setRegion(region_chooser(current_data));
                player.setServer(server_chooser(current_data, player.getRegion()));
                player.setName(JOptionPane.showInputDialog("Enter player name: "));
                break;
            case -1: throw new Exception("Operation canceled");
        }
        if(current_data.isPlayer_Valid(player)){
            current_data.putIn_Map(ID, player);
            current_data.setFile_changed(true);
        }else{
            throw new Exception("Player data is invalid");
        }
    }

    private static void xmlTo_dat(){

    }


    private static void create_player(PlayerOperationData current_data) throws Exception {
        Player player = new Player();
        player.setRegion(region_chooser(current_data));
        player.setServer(server_chooser(current_data, player.getRegion()));
        player.setID(create_ID(current_data));
        player.setName(JOptionPane.showInputDialog("Enter Player Name: "));
        if(current_data.isPlayer_Valid(player)) {
            current_data.getPlayer_data().put(player.getID(), player);
            current_data.setFile_changed(true);
        }else{
            throw new Exception("Player data is invalid");
        }
    }

    public static int create_ID(PlayerOperationData current_data)  {
        while (true) {
            try {
                int ID = Integer.parseInt(JOptionPane.showInputDialog("Input ID number: "));
                if (current_data.containsKey(ID)) {
                    JOptionPane.showMessageDialog(null, "ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,"ID format incorrect");
            }
        }
    }

    public  static String region_chooser(PlayerOperationData current_data) throws Exception {
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
}
