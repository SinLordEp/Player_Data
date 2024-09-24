package utils;

import model.PersonOperationData;

public class FileWriterFactory {
    public static FileWriter getFileWriter(Object current_data) throws Exception {
        if(current_data instanceof PersonOperationData){
            return switch (((PersonOperationData) current_data).getFile_extension()) {
                case "dat" -> new DATWriter((PersonOperationData) current_data);
                case "xml" -> new XMLWriter((PersonOperationData) current_data);
                default -> throw new Exception("File type not supported");
            };
        }
        // data class not matched
        throw new Exception("Data class not supported");
    }
}
