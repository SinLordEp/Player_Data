package Interface;

import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;

import java.util.HashMap;

public interface GeneralControl {
    void run() throws Exception;
    void setDA(GeneralDataAccess DA);
    void onWindowClosing();
    void connectDB();
    void setDataSource(DataSource dataSource);
    void setSQLDialect(SqlDialect dialect);
    void setFileType(FileType fileType);
}
