package utils;

import model.GeneralOperationData;
import model.PlayerOperationData;

public class DataReaderFactory {
    public static void initialReader(GeneralOperationData current_data) throws Exception {
        if(current_data instanceof PlayerOperationData){
            current_data.setReader(new PlayerReader());
        }else{
            // data class not matched
            throw new Exception("Data class not supported in DataReaderFactory");
        }
    }
}
