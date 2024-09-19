package main;

import model.Person;
import model.Player;
import utils.Dat_Reader;
import utils.File_Chooser;

import javax.swing.*;
import java.io.File;
import java.util.Map;

public class principal {
    static Map<Integer, Person> person;
    static File file = null;
    static void menu(){
        String type = "";
        while(type.isEmpty()){
            try{
                switch(Integer.parseInt(JOptionPane.showInputDialog( """
                Choose a database source:
                1. Dat File
                2. XML File
                0. Exit"""))){
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
        while(true){
            try{
                if(file == null || !file.getName().substring(file.getName().lastIndexOf('.')+1).equals(type)){
                    file = File_Chooser.get_path(type);
                }
                switch(Integer.parseInt(JOptionPane.showInputDialog( """
                Choose a operation:
                1. Read File
                2. Write File
                3. Back to menu
                0. Exit"""))){
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
            }
        }
    }

    static void reading_page(String type) throws Exception{
        switch (type){
            case "dat":
                person = new Dat_Reader(file).read_person();
                break;
            case "xml":
                break;
        }
    }
    static void show_data(){
        for(Person temp : person.values()){
            JOptionPane.showMessageDialog(null, temp.toString());
        }
    }
    static void writing_page(String type)throws Exception{
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

    static Person create_person(String type) throws Exception {
        Person temp = null;
        try{
            switch(Integer.parseInt(JOptionPane.showInputDialog( """
                Choose type of person:
                1. Player
                2. GM (not available)
                0. Cancel"""))){
                case 1: temp = new Player();break;
                case 2:
                case 0: break;
                default: throw new Exception("Option invalid");
            }
            if(temp == null) throw new Exception("Operation canceled");
            temp.setRegion(region_chooser());
            temp.setServer(server_chooser());
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
    static String region_chooser(){
        String region = "";
        while(true){
            try{
                region = switch (Integer.parseInt(JOptionPane.showInputDialog("""
                        Choose a region:
                        1. EU
                        2. Asia
                        3. North America
                        0. Cancel"""))) {
                    case 1 -> "EU";
                    case 2 -> "Asia";
                    case 3 -> "North America";
                    case 0 -> "null";
                    default -> throw new Exception("Option invalid");
                };
                if(region.equals("null")) {
                    throw new Exception("Operation canceled");
                }else{
                    return region;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    static String server_chooser(){
     return "";
    }
    public static void main(String[] args) {
        menu();


    }
}
