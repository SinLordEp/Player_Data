package main;

import model.FileOperationData;

import static main.principal.buildSelectionDialog;

public class process_read {
    static String read(FileOperationData current_data) throws Exception{
        String[] person_class = {"Player", "GM (Currently Not Available)"};
        String person_type = switch (buildSelectionDialog("Choose a person type", person_class)){
            case 1 -> person_class[0];
            case 2 -> throw new Exception("Currently Not Available");
            case -1 -> throw new Exception("Operation canceled");
            default -> throw new Exception("Option invalid");
        };
        current_data.setPerson_data();
        return person_type;
    }

}
