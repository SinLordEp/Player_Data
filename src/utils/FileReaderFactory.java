package utils;

import model.PersonOperationData;

public class FileReaderFactory {
    public static FileReader<Object> getFileReader(Object current_data) throws Exception {
        if(current_data instanceof PersonOperationData){
            return switch (((PersonOperationData) current_data).getFile_extension()) {
                case "dat" -> new DATReader((PersonOperationData) current_data);
                case "xml" -> new XMLReader((PersonOperationData) current_data);
                default -> throw new Exception("File type not supported");
            };
        }
        // data class not matched
        throw new Exception("Data class not supported");
    }
}
