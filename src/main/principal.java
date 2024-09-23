package main;

import model.FileOperationData;
import utils.*;

import javax.swing.*;

import static main.process_read.*;
import static main.process_write.*;

public class principal {

    public static void menu(FileOperationData current_data){
        String[] options = {"Dat File", "XML File"};
        while(current_data.getFile_type().isEmpty()){
            try{
                current_data.setFile_type(switch (buildSelectionDialog("Choose a data source", options)) {
                    case 1 -> "dat";
                    case 2 -> "xml";
                    case -1 -> "Exit";
                    default -> "";
                });
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        if(current_data.getFile_type().equals("Exit")) System.exit(0);
        second_menu(current_data);
    }

    public static void second_menu(FileOperationData current_data){
        String[] options = {"Show Data", "Write File", "Convert to .dat File", "Back to menu"};
        do{
            try{
                if(current_data.isFile_valid()){
                    JOptionPane.showMessageDialog(null,"Choosing source file");
                    current_data.setPerson_file(File_Chooser.get_path(current_data.getFile_type())) ;
                    current_data.setFile_changed(true);
                }
                if(current_data.isFile_changed() || current_data.getPerson_data() == null) {
                    current_data.setPerson_type(read_file(current_data));
                    current_data.setFile_changed(false);
                }
                switch(buildSelectionDialog("""
                        Path: %s
                        Person Type: %s
                        Choose a operation""".formatted(current_data.getAbsolutePath(), current_data.getPerson_type()), options)){
                    case 1: current_data.print_person(); break;
                    case 2: update(current_data);
                        if(current_data.isFile_changed()) writing_process(current_data);
                        break;
                    case 3: convert_toDAT(); break;
                    case -1,4: menu(current_data);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }while(!current_data.isFile_changed());
        //when file has changed, clear data and back to menu
        current_data.setPerson_file(null);
        menu(current_data);
    }

    public static int buildSelectionDialog(String title, String[] options) {
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
        FileOperationData current_data = new FileOperationData();
        menu(current_data);
    }
}
