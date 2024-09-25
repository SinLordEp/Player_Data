package utils;

import data.GeneralData;
import data.PlayerData;

public class DataWriterFactory {
    public static void initializeWriter(GeneralData current_data) throws Exception {
        if(current_data instanceof PlayerData){
            current_data.setWriter(new PlayerWriter());
        }else{
            // data class not matched
            throw new Exception("Data class not supported in DataWriterFactory");
        }
    }
}
