package Interface;

import data.DataSource;

/**
 * @author SIN
 */
public interface GeneralControl {
    void run() throws Exception;
    void setDA(GeneralDataAccess DA);
    void onWindowClosing();
    boolean connectDB();
    void setDataSource(DataSource dataSource);
}
