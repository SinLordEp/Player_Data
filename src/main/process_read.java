package main;

import model.FileOperationData;
import utils.Dat_Reader;
import utils.XML_Reader;

import java.io.IOException;

import static main.principal.buildSelectionDialog;

public class process_read {
    public static String read_file(FileOperationData current_data) throws Exception{
        String[] person_class = {"Player", "GM (Currently Not Available)"};
        String person_type = switch (buildSelectionDialog("Choose a person type", person_class)){
            case 1 -> person_class[0];
            case 2 -> throw new Exception("Currently Not Available");
            case -1 -> throw new Exception("Operation canceled");
            default -> throw new Exception("Option invalid");
        };
        setPerson_data(current_data);
        return person_type;
    }

    public static void setPerson_data(FileOperationData current_data) throws Exception {
        switch (current_data.getFile_type()){
            case "dat": readFrom_dat(current_data); break;
            case "xml": readFrom_xml(current_data); break;
        }
    }

    public static void readFrom_dat(FileOperationData current_data) throws IOException, ClassNotFoundException {
        new Dat_Reader(current_data.getPerson_file()).parse_person();
    }

    public static void readFrom_xml(FileOperationData current_data) throws Exception {
        XML_Reader xml_reader = new XML_Reader();
        switch(current_data.getPerson_type()){
            case "Player":
                current_data.setPerson_data(xml_reader.parse_player(xml_reader.file_reading(current_data.getPerson_file())));
                break;
            case "GM": break;
        }
        if(current_data.getPerson_data() == null){
            throw new Exception("File does not match the chosen person type");
        }
    }
}
