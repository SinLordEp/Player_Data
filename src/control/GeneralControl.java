package control;

import data.GeneralDataAccess;

public interface GeneralControl<T extends GeneralDataAccess> {
    void run() throws Exception;
    void setDA(GeneralDataAccess DA);
    T getDA();
}
