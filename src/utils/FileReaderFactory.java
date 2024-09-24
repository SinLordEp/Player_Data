package utils;

import model.PlayerOperationData;

public class FileReaderFactory {
    public static FileReader getFileReader(Object current_data) throws Exception {
        if(current_data instanceof PlayerOperationData){
            return switch (((PlayerOperationData) current_data).getFile_extension()) {
                case "dat" -> new DATReader((PlayerOperationData) current_data);
                case "xml" -> new XMLReader((PlayerOperationData) current_data);
                default -> throw new Exception("File type not supported");
            };
        }
        // data class not matched
        throw new Exception("Data class not supported");
    }
}
