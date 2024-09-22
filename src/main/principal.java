package main;

import model.Person;
import model.Player;
import utils.Dat_Reader;
import utils.File_Chooser;
import utils.XML_Reader;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class principal {
    static Map<Integer, Person> person_data = null;
    static Map<String,String[]> region_server = null;
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
                    default -> "";
                };
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        second_menu(file_type);
    }
    static void second_menu(String file_type){
        String[] options = {"Show Data", "Write File", "Back to menu"};
        while(true){
            try{
                if(person_file == null || !person_file.getName().substring(person_file.getName().lastIndexOf('.')+1).equals(file_type)){
                    JOptionPane.showMessageDialog(null,"Choosing source file");
                    person_file = File_Chooser.get_path(file_type);
                    file_changed = true;
                }
                if(file_changed || person_data == null) {
                    reading_process(file_type);
                    file_changed = false;
                }
                switch(buildSelectionDialog("Current path: \n%s\nCurrent data type: %s\nChoose a operation".formatted(person_file.getAbsolutePath(),""), options)){
                    case 1: show_data(); break;
                    case 2: writing_process(file_type); break;
                    case 3: return;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                person_file = null;
                menu();
            }
        }
    }

    static void reading_process(String file_type) throws Exception{
        switch (file_type){
            case "dat":
                Dat_Reader dat_reader = new Dat_Reader(person_file);
                person_data = dat_reader.parse_person();
                person_data = new Dat_Reader(person_file).parse_person();
                break;
            case "xml":
                String[] person_type = {"Player", "GM (Currently Not Available)"};
                XML_Reader xml_reader = new XML_Reader();
                switch(buildSelectionDialog("Choose a person type", person_type)){
                    case 1: person_data = xml_reader.parse_player(xml_reader.file_reading(person_file)); break;
                    case 2: throw new Exception("Currently Not Available");
                }
        }
    }
    static void show_data(){
        for(Person temp : person_data.values()){
            JOptionPane.showMessageDialog(null, temp.toString());
        }
    }
    static void writing_process(String file_type){
        Person temp = create_person();
        if(temp == null) return;
        person_data.put(temp.getID(), temp);
        switch (file_type){
            case "dat":
                //person = new Dat_Writer(file);
                break;
            case "xml":
                break;
        }

    }

    static Person create_person() {
        Person temp = null;
        String[] options = {"Player", "Back to previous menu"};
        try{
            temp = switch (buildSelectionDialog("Choose type of person: ", options)) {
                case 1 -> new Player();
                case 2 -> null;
                default -> throw new Exception("Option invalid");
            };
            if(temp == null) throw new Exception("Operation canceled");
            if(region_server == null) update_region_server();
            temp.setRegion(region_chooser());
            temp.setServer(server_chooser(temp.getRegion()));
            temp.setID(ID_verify());
            temp.setName(JOptionPane.showInputDialog("Input person Name: "));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return temp;
    }

    static int ID_verify()  {
        while(true){
            int ID = Integer.parseInt(JOptionPane.showInputDialog("Input ID number: "));
            if(person_data.containsKey(ID)){
                JOptionPane.showMessageDialog(null,"ID already exists");
            }else return ID;
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
        int option = buildSelectionDialog("Choose a region: " ,regionList.toArray(new String[0]));
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
                int option = Integer.parseInt(JOptionPane.showInputDialog(dialog));
                if (option == 0) {
                    System.exit(0);
                } else if (option > 0 && option <= options.length) {
                    return option;
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Option invalid");
            }
        }
    }
    public static void main() {
        menu();
    }
}
