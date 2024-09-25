package utils;

import data.GeneralData;

public interface DataWriter {
    void write(GeneralData current_data) throws Exception;
    void export(String file_extension, GeneralData current_data ) throws Exception;
}
