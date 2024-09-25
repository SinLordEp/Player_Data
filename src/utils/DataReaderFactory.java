package utils;

import data.GeneralData;
import data.PlayerData;

public class DataReaderFactory {
    public static void initialReader(GeneralData current_data) throws Exception {
        if(current_data instanceof PlayerData){
            current_data.setReader(new PlayerReader());
        }else{
            // data class not matched
            throw new Exception("Data class not supported in DataReaderFactory");
        }
    }
}
