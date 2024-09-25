package utils;

import model.GeneralOperationData;

public interface DataWriter {
    void write(GeneralOperationData current_data) throws Exception;
}
