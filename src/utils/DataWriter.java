package utils;

import model.GeneralOperationData;

public interface DataWriter {
    void write(GeneralOperationData current_data) throws Exception;
    void export(String file_extension, GeneralOperationData current_data ) throws Exception;
}
