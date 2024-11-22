package Interface;

import data.DataSource;
import data.database.SqlDialect;

import java.util.HashMap;

public interface GeneralControl {
    void run() throws Exception;
    void setDA(GeneralDataAccess DA);
    void onWindowClosing();
    HashMap<String, String> getDefaultDatabase();
    void connectDB();
    void setDataSource(DataSource dataSource);
    void setSQLDialect(SqlDialect dialect);
}
