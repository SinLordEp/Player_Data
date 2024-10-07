package GUI;

import data.GeneralDataAccess;

public interface GeneralUI {
    GeneralDataAccess DATA_ACCESS = null;
    void run();
    void close();
    void refresh() throws Exception;
}
