package utils;

import model.GeneralOperationData;
import model.PlayerOperationData;

public class DataWriterFactory {
    public static void initializeWriter(GeneralOperationData current_data) throws Exception {
        if(current_data instanceof PlayerOperationData){
            current_data.setWriter(new PlayerWriter());
        }else{
            // data class not matched
            throw new Exception("Data class not supported in DataWriterFactory");
        }
    }
}
