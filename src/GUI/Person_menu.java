package GUI;

import model.Person;
import model.PersonOperationData;
import model.Player;
import utils.*;

import javax.swing.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static GUI.GUI_utils.buildSelectionDialog;


public class Person_menu {

    public static void person_menu(PersonOperationData current_data){
        String[] options = {"Dat File", "XML File"};
        while(current_data.isFile_valid()){
            try{
                current_data.setFile_extension(switch (buildSelectionDialog("Choose a data source", options)) {
                    case 1 -> "dat";
                    case 2 -> "xml";
                    case -1 -> throw new Exception("Operation canceled");
                    default -> "";
                });
                // Choose file
                JOptionPane.showMessageDialog(null,"Choosing source file");
                current_data.setFile(File_Chooser.get_path(current_data.getFile_extension())) ;
                if(current_data.isFile_valid()) throw new Exception("File is not valid");
                current_data.setFile_changed(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")){
                    current_data = null;
                    return;
                }
            }
        }
        second_menu(current_data);
    }

    public static void second_menu(PersonOperationData current_data) throws Exception{
        String[] person_class = {"Player", "GM (Currently Not Available)"};
        current_data.setPerson_type(switch (buildSelectionDialog("Choose a person type", person_class)){
            case 1 -> person_class[0];
            case 2 -> throw new Exception("Currently Not Available");
            case -1 -> throw new Exception("Operation canceled");
            default -> throw new Exception("Option invalid");
        });
        FileReader<Object> fileReader = FileReaderFactory.getFileReader(current_data);
    }

    public static void third_menu(PersonOperationData current_data){
        String[] options = {"Show Data", "Write File", "Convert to .dat File", "Back to menu"};
        try{
            switch(buildSelectionDialog("""
                        Path: %s
                        Person Type: %s
                        Choose a operation""".formatted(current_data.getAbsolutePath(), current_data.getPerson_type()), options)){
                case 1: current_data.print_person(); break;
                case 2: update(current_data);
                    if(current_data.isFile_changed()) writing_process(current_data);
                    break;
                case 3: convert_toDAT(); break;
                case -1,4: person_menu(current_data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }


    public static void update(PersonOperationData current_data) throws Exception {
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
    public static void convert_toDAT(){

    }

    public static void modify_person(PersonOperationData current_data){
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

    public static boolean modify_player(PersonOperationData current_data) throws Exception {
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

    public static void writing_process(PersonOperationData current_data) throws Exception {
        switch (current_data.getFile_extension()){
            case "dat": writeTo_dat(current_data); break;
            case "xml": writeTo_xml(current_data); break;
        }
    }

    public static Person create_person(PersonOperationData current_data) throws Exception {
        if(current_data.getRegion_server() == null) update_region_server(current_data);
        return switch (current_data.getPerson_type()) {
            case "Player" -> create_player(current_data);
            case "GM" -> null;
            default -> throw new Exception("Person type invalid");
        };
    }

    public static Player create_player(PersonOperationData current_data) throws Exception {
        Player player = new Player();
        player.setRegion(region_chooser(current_data));
        player.setServer(server_chooser(current_data, player.getRegion()));
        player.setID(create_ID(current_data));
        player.setName(JOptionPane.showInputDialog("Enter person Name: "));
        return player;
    }

    public static int create_ID(PersonOperationData current_data)  {
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
    public static void writeTo_dat(PersonOperationData current_data) throws IOException {
        new DATWriter(current_data.getFile()).write_person(current_data.getPerson_data());
    }

    public static void writeTo_xml(PersonOperationData current_data){
        new XMLWriter().update_Person(current_data);
    }
    public static void update_region_server(PersonOperationData current_data) {
        XMLReader reader = new XMLReader();
        File temp = new File("./src/main/servers.xml");
        current_data.setRegion_server(reader.parse_region_server(reader.file_reading(temp)));
    }

    public  static String region_chooser(PersonOperationData current_data) throws Exception {
        Set<String> regions = current_data.getRegion_server().keySet();
        List<String> regionList = new ArrayList<>(regions);
        int option = buildSelectionDialog("Choose a region: " , regionList.toArray(new String[0]));
        if(option == -1) throw new Exception("Operation canceled");
        return regionList.get(option-1);
    }

    public static String server_chooser(PersonOperationData current_data, String region) throws Exception {
        String[] servers = current_data.getServer(region);
        int option = buildSelectionDialog("Choose a server: ", servers);
        if(option == -1) throw new Exception("Operation canceled");
        return servers[option-1];
    }
}
