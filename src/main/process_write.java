package main;

import model.FileOperationData;
import model.Person;
import model.Player;
import utils.XML_Reader;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static main.principal.buildSelectionDialog;


public class process_write {
    static void update(FileOperationData current_data) throws Exception {
        String[] options = {"Create new " + current_data.getPerson_type(), "Modify data"};

        switch(buildSelectionDialog("Choose a operation", options)){
            case 1:
                Person temp = create_person(current_data);
                if(temp == null) return;
                current_data.getPerson_data().put(temp.getID(), temp);
                current_data.setFile_changed(true);
                break;
            case 2:
                modify_person(current_data);
                break;
            case -1: throw new Exception("Operation canceled");
        }
    }
    static void convert_toDAT(){

    }

    static void modify_person(FileOperationData current_data){
        try{
            current_data.setFile_changed(switch(current_data.getPerson_type()){
                case "Player" -> modify_player(current_data);
                case "GM" -> true;
                default -> false;
            });
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,e.getMessage());
        }
    }

    static boolean modify_player(FileOperationData current_data) throws Exception {
        String[] options = {"Region", "Server", "Name", "ALL"};
        int ID = Integer.parseInt(JOptionPane.showInputDialog("Enter the ID to modify"));
        if(!current_data.containsKey(ID)){
            JOptionPane.showMessageDialog(null,"Person does not exist");
            return false;
        }
        Person temp = current_data.getFrom_Map(ID);
        switch(buildSelectionDialog("Choose the data you need to modify", options)){
            // case 1 is linked to case 2, because after changing region the server has to be changed too.
            case 1: temp.setRegion(region_chooser(current_data));
            case 2: temp.setServer(server_chooser(current_data, temp.getRegion())); break;
            case 3: temp.setName(JOptionPane.showInputDialog("Enter player name: ")); break;
            case 4: temp.setRegion(region_chooser(current_data));
                temp.setServer(server_chooser(current_data, temp.getRegion()));
                temp.setName(JOptionPane.showInputDialog("Enter player name: "));
                break;
            case -1: return false;
        }
        current_data.putIn_Map(ID, temp);
        return true;
    }

    static void writing_process(FileOperationData current_data) throws Exception {
        switch (current_data.getFile_type()){
            case "dat": current_data.writeTo_dat(); break;
            case "xml": current_data.writeTo_xml(); break;
        }
    }

    static Person create_person(FileOperationData current_data) throws Exception {
        if(current_data.getRegion_server() == null) update_region_server(current_data);
        return switch (current_data.getPerson_type()) {
            case "Player" -> create_player(current_data);
            case "GM" -> null;
            default -> throw new Exception("Person type invalid");
        };
    }

    static Player create_player(FileOperationData current_data) throws Exception {
        Player player = new Player();
        player.setRegion(region_chooser(current_data));
        player.setServer(server_chooser(current_data, player.getRegion()));
        player.setID(create_ID(current_data));
        player.setName(JOptionPane.showInputDialog("Enter person Name: "));
        return player;
    }

    static int create_ID(FileOperationData current_data)  {
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

    static void update_region_server(FileOperationData current_data) {
        XML_Reader reader = new XML_Reader();
        File temp = new File("./src/main/servers.xml");
        current_data.setRegion_server(reader.parse_region_server(reader.file_reading(temp)));
    }
    static String region_chooser(FileOperationData current_data) throws Exception {
        Set<String> regions = current_data.getRegion_server().keySet();
        List<String> regionList = new ArrayList<>(regions);
        int option = buildSelectionDialog("Choose a region: " , regionList.toArray(new String[0]));
        if(option == -1) throw new Exception("Operation canceled");
        return regionList.get(option-1);
    }

    static String server_chooser(FileOperationData current_data, String region) throws Exception {
        String[] servers = current_data.getServer(region);
        int option = buildSelectionDialog("Choose a server: ", servers);
        if(option == -1) throw new Exception("Operation canceled");
        return servers[option-1];
    }
}
