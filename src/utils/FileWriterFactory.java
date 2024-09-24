package utils;

import model.GeneralOperationData;
import model.PlayerOperationData;

public class FileWriterFactory {
    public static FileWriter getFileWriter(Object current_data) throws Exception {
        if(current_data instanceof PlayerOperationData){
            return switch (((PlayerOperationData) current_data).getFile_extension()) {
                case "dat" -> new DATWriter((PlayerOperationData) current_data);
                case "xml" -> new XMLWriter((PlayerOperationData) current_data);
                default -> throw new Exception("File type not supported");
            };
        }
        // data class not matched
        throw new Exception("Data class not supported");
    }
}
