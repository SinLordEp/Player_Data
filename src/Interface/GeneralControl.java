package Interface;

/**
 * @author SIN
 */
public interface GeneralControl {
    void run();
    void setDA(GeneralDataAccess DA);
    void onWindowClosing();
}
