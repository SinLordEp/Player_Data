package control;

import data.GeneralDataAccess;

public interface GeneralControl {
    void run() throws Exception;
    void setDA(GeneralDataAccess DA);
}
