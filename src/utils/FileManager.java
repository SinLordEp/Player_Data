package utils;

import model.GeneralOperationData;

public class FileManager {

    public void readData(GeneralOperationData current_Data) throws Exception {
        if(current_Data.getReader() != null){
            current_Data.getReader().read(current_Data);
        }else{
            throw new IllegalStateException("Reader is not initialized");
        }
    }

    public void writeData(GeneralOperationData currentData) throws Exception {
        if(currentData.getWriter() != null){
            currentData.getWriter().write(currentData);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }
}
