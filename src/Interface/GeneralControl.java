package Interface;

import data.GeneralDataAccess;

/**
 * @author SIN
 */
public interface GeneralControl {
    void run();
    void setDA(GeneralDataAccess DA);
    void onWindowClosing();
}
