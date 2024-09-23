package main;

import model.Person;
import model.Player;
import utils.*;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class principal {
    static Map<Integer, Person> person_data = null;
    static Map<String, String[]> region_server = null;
    static File person_file = null;
    static boolean file_changed = false;
    static void menu(){
        String file_type = "";
        String[] options = {"Dat File", "XML File"};
        while(file_type.isEmpty()){
            try{
                file_type = switch (buildSelectionDialog("Choose a data source", options)) {
                    case 1 -> "dat";
                    case 2 -> "xml";
                    case -1 -> "Exit";
                    default -> "";
                };
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        if(file_type.equals("Exit")) System.exit(0);
        second_menu(file_type);
    }
    static void second_menu(String file_type){
        String[] options = {"Show Data", "Write File", "Back to menu"};
        String person_type = "";
        while(true){
            try{
                if(person_file == null || !person_file.getName().substring(person_file.getName().lastIndexOf('.')+1).equals(file_type)){
                    JOptionPane.showMessageDialog(null,"Choosing source file");
                    person_file = File_Chooser.get_path(file_type);
                    file_changed = true;
                }
                if(file_changed || person_data == null) {
                    person_type = reading_process(file_type);
                    file_changed = false;
                }
                switch(buildSelectionDialog("""
                        Current path:
                        %s
                        Current data type:
                        %s
                        Choose a operation""".formatted(person_file.getAbsolutePath(), person_type), options)){
                    case 1: show_data(); break;
                    case 2: update_process(person_type);
                        if(file_changed) writing_process(file_type, person_type);
                        break;
                    case -1,3: return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                person_file = null;
                menu();
            }
        }
    }

    static String reading_process(String file_type) throws Exception{
        String[] person_class = {"Player", "GM (Currently Not Available)"};
        String person_type = switch (buildSelectionDialog("Choose a person type", person_class)){
            case 1 -> person_class[0];
            case 2 -> throw new Exception("Currently Not Available");
            case -1 -> throw new Exception("Operation canceled");
            default -> throw new Exception("Option invalid");
        };
        switch (file_type){
            case "dat":
                Dat_Reader dat_reader = new Dat_Reader(person_file);
                person_data = dat_reader.parse_person();
                person_data = new Dat_Reader(person_file).parse_person();
                break;
            case "xml":
                XML_Reader xml_reader = new XML_Reader();
                switch(person_type){
                    case "Player":
                        person_data = xml_reader.parse_player(xml_reader.file_reading(person_file));
                        break;
                    case "GM": break;
                }
                if(person_data == null){
                    JOptionPane.showMessageDialog(null,"File does not match the chosen person type");
                    menu();
                }
        }
        return person_type;
    }

    static void show_data(){
        for(Person temp : person_data.values()){
            JOptionPane.showMessageDialog(null, temp.toString());
        }
    }

    static void update_process(String person_type) throws Exception {
        String[] options = {"Create new " + person_type, "Modify " + person_type};
        switch(buildSelectionDialog("Choose a operation", options)){
            case 1:
                Person temp = create_person(person_type);
                if(temp == null) return;
                person_data.put(temp.getID(), temp);
                file_changed = true;
                break;
            case 2:
                modify_person(person_type);
                break;
            case -1:
                break;
        }
    }

    static void modify_person(String person_type){
        try{
            file_changed = switch(person_type){
                case "Player" -> modify_player();
                case "GM" -> true;
                default -> false;
            };
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,e.getMessage());
        }
    }

    static boolean modify_player() {
        String[] options = {"Region", "Server", "Name", "ALL"};
        int ID = Integer.parseInt(JOptionPane.showInputDialog("Enter the ID to modify"));
        if(!person_data.containsKey(ID)){
            JOptionPane.showMessageDialog(null,"Person does not exist");
            return false;
        }
        Person temp = person_data.get(ID);
        switch(buildSelectionDialog("Choose the data you need to modify", options)){
            case 1: temp.setRegion(region_chooser());
            case 2: temp.setServer(server_chooser(temp.getRegion())); break;
            case 3: temp.setName(JOptionPane.showInputDialog("Enter player name: ")); break;
            case 4: temp.setRegion(region_chooser());
                temp.setServer(server_chooser(temp.getRegion()));
                temp.setName(JOptionPane.showInputDialog("Enter player name: "));
                break;
            case -1: return false;
        }
        person_data.put(ID,temp);
        return true;
    }

    static void writing_process(String file_type, String person_type) throws Exception {
        switch (file_type){
            case "dat":
                new Dat_Writer(person_file).write_person(person_data);
                break;
            case "xml":
                new XML_Writer().update_Person(person_file, person_type, person_data);
                break;
        }
    }

    static Person create_person(String person_type) throws Exception {
        if(region_server == null) update_region_server();
        return switch (person_type) {
            case "Player" -> create_player();
            case "GM" -> null;
            default -> throw new Exception("Person type invalid");
        };
    }

    static Player create_player(){
        Player player = new Player();
        player.setRegion(region_chooser());
        player.setServer(server_chooser(player.getRegion()));
        player.setID(create_ID());
        player.setName(JOptionPane.showInputDialog("Enter person Name: "));
        return player;
    }

    static int create_ID()  {
        while (true) {
            try {
                int ID = Integer.parseInt(JOptionPane.showInputDialog("Input ID number: "));
                if (person_data.containsKey(ID)) {
                    JOptionPane.showMessageDialog(null, "ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,"ID format incorrect");
            }
        }
    }

    static void update_region_server() {
        XML_Reader reader = new XML_Reader();
        File temp = new File("./src/main/servers.xml");
        region_server = reader.parse_region_server(reader.file_reading(temp));
    }

    static String region_chooser() {
        Set<String> regions = region_server.keySet();
        List<String> regionList = new ArrayList<>(regions);
        int option = buildSelectionDialog("Choose a region: " , regionList.toArray(new String[0]));
        return regionList.get(option-1);
    }

    static String server_chooser(String region) {
        String[] servers = region_server.get(region);
        int option = buildSelectionDialog("Choose a server: ", servers);
        return servers[option-1];
    }

    static int buildSelectionDialog(String title, String[] options) {
        StringBuilder dialog = new StringBuilder(title);
        int i = 1;
        for (String option : options) {
            dialog.append("\n").append(i++).append(". ").append(option);
        }
        dialog.append("\n0. Exit");

        while (true) {
            try {
                String option_string = JOptionPane.showInputDialog(dialog);
                if(option_string == null){
                    return -1;
                }
                if(option_string.equals("0")) System.exit(0);

                int option_int = Integer.parseInt(option_string);
                if (option_int > 0 && option_int <= options.length) {
                    return option_int;
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Option invalid");
            }
        }
    }
    public static void main(String[] args) {
        menu();
    }
}
