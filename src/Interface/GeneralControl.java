package Interface;

import data.GeneralDAO;

/**
 * @author SIN
 */
public interface GeneralControl {
    void run();
    void setDA(GeneralDAO DA);
    void onWindowClosing();
}
