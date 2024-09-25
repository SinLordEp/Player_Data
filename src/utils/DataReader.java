package utils;

import model.GeneralOperationData;

public interface DataReader {
    void read(GeneralOperationData current_data) throws Exception;
}
