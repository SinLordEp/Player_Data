package Interface;

import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;

public interface GeneralControl {
    void run() throws Exception;
    void setDA(GeneralDataAccess DA);
    void onWindowClosing();
    boolean connectDB();
    void setDataSource(DataSource dataSource);
    void setSQLDialect(SqlDialect dialect);
    void setFileType(FileType fileType);
}
