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
    static Map<Integer, Person> person;
    static Map<String,String[]> region_server = null;
    static File file = null;
    static void menu(){
        String type = "";
        String[] dialog = {"Dat File","XML File"};
        while(type.isEmpty()){
            try{
                switch(showSelectionDialog("Choose a database source", dialog)){
                    case 1: type = "dat";break;
                    case 2: type = "xml";break;
                    case 0: System.exit(0);
                    default: throw new Exception("Option invalid");
                }
                second_menu(type);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }
    static void second_menu(String type){
        String[] dialog = {"Read File","Write File","Back to menu"};
        while(true){
            try{
                if(file == null || !file.getName().substring(file.getName().lastIndexOf('.')+1).equals(type)){
                    JOptionPane.showMessageDialog(null,"Choosing source file");
                    file = File_Chooser.get_path(type);
                }
                switch(showSelectionDialog("Current data source path: \n"+ file.getAbsolutePath() +"\n\nChoose a operation", dialog)){
                    case 1: reading_page(type);
                        show_data();
                        break;
                    case 2: writing_page(type); break;
                    case 3: menu(); break;
                    case 0: System.exit(0);
                    default: throw new Exception("Option invalid");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                file = null;
                menu();
            }
        }
    }

    static void reading_page(String type) throws Exception{
        switch (type){
            case "dat":
                person = new Dat_Reader(file).parse_player();
                break;
            case "xml":
                XML_Reader reader = new XML_Reader();
                person = reader.parse_player(reader.file_reading(file));
                break;
        }
    }
    static void show_data(){
        for(Person temp : person.values()){
            JOptionPane.showMessageDialog(null, temp.toString());
        }
    }
    static void writing_page(String type){
        Person temp = create_person(type);
        person.put(temp.getID(), temp);
        switch (type){
            case "dat":
                //person = new Dat_Writer(file);
                break;
            case "xml":
                break;
        }

    }

    static Person create_person(String type) {
        Person temp = null;
        String[] dialog = {"Player","Back to previous menu"};
        try{
            switch(showSelectionDialog("Choose type of person: ", dialog)){
                case 1: temp = new Player();break;
                case 2: second_menu(type);
                case 0: break;
                default: throw new Exception("Option invalid");
            }
            if(temp == null) throw new Exception("Operation canceled");
            if(region_server == null) update_region_server();
            temp.setRegion(region_chooser());
            temp.setServer(server_chooser(temp.getRegion()));
            temp.setID(ID_verify(type));
            temp.setName(JOptionPane.showInputDialog("Input person Name: "));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return temp;
    }

    static int ID_verify(String type) throws Exception {
        reading_page(type);
        while(true){
            int ID = Integer.parseInt(JOptionPane.showInputDialog("Input ID number: "));
            if(person.containsKey(ID)){
                JOptionPane.showMessageDialog(null,"ID already exists");
            }else return ID;
        }
    }

    static void update_region_server() {
        XML_Reader reader = new XML_Reader();
        File temp = new File("./src/main/servers.xml");
        region_server = reader.parse_region_server(reader.file_reading(temp));
    }

    static String region_chooser() throws Exception {
        Set<String> regions = region_server.keySet();
        List<String> regionList = new ArrayList<>(regions);
        int option = showSelectionDialog("Choose a region: " ,regionList.toArray(new String[0]));
        return regionList.get(option-1);
    }

    static String server_chooser(String region) throws Exception {
        String[] servers = region_server.get(region);
        int option = showSelectionDialog("Choose a server: ", servers);
        return servers[option-1];
    }
    static int showSelectionDialog(String title, String[] options) throws Exception {
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
                    throw new Exception("Option invalid");
                }
            } catch (RuntimeException e) {
                throw new Exception(e.getMessage());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }
    public static void main(String[] args) {
        menu();


    }
}
