package Interface;

import GUI.GeneralText;
import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;
import exceptions.ConfigErrorException;
import exceptions.FileManageException;
import exceptions.OperationCancelledException;
import model.DatabaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

import static main.principal.getProperty;


/**
 * @author SIN
 */
public abstract class GeneralDataAccess {
    protected String file_path = null;
    protected DataSource dataSource = DataSource.NONE;
    protected FileType fileType = FileType.NONE;
    protected DatabaseInfo databaseInfo = new DatabaseInfo();
    public abstract void read();
    public abstract void save();
    public abstract DatabaseInfo getDefaultDatabaseInfo(SqlDialect sqlDialect) throws ConfigErrorException;
    private static final Logger logger = LoggerFactory.getLogger(GeneralDataAccess.class);

    public void setFilePath(String file_path) {
        this.file_path = file_path;
        logger.info("Set file path: Path is set to {}", file_path);
        this.dataSource = DataSource.FILE;
        logger.info("Set file path: DataSource is set to: File");
    }

    public static String getExtension(FileType fileType) {
        logger.info("Get extension: Getting extension for {}", fileType);
        return switch (fileType) {
            case FileType.DAT -> ".dat";
            case FileType.XML -> ".xml";
            case FileType.TXT -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    public static String getPath(FileType fileType) {
        logger.info("Get path: Getting path with file type paragram: {}", fileType);
        JFileChooser fileChooser = new JFileChooser(new File(getProperty("defaultFilePath")).getAbsolutePath());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choosing " + fileType);
        switch(fileType){
            case TXT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(GeneralText.getDialog().getText("extension_txt"), "txt"));
                break;
            case DAT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(GeneralText.getDialog().getText("extension_dat"), "dat"));
                break;
            case XML:
                fileChooser.setFileFilter(new FileNameExtensionFilter(GeneralText.getDialog().getText("extension_xml"), "xml"));
                break;
        }
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            logger.info("Get path: Finished get path with paragram!");
            return fileChooser.getSelectedFile().getPath();
        }else {
            throw new OperationCancelledException();
        }
    }

    public static String getPath(){
        logger.info("Get path: Getting path without paragram...");
        JFileChooser fileChooser = new JFileChooser(new File(getProperty("defaultFilePath")).getAbsolutePath());
        fileChooser.setDialogTitle("Choosing path");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            logger.info("Get path: Finished get path without paragram!");
            return fileChooser.getSelectedFile().getPath();
        }else{
            throw new OperationCancelledException();
        }
    }

    public static String newPathBuilder(FileType fileType) {
        logger.info("New path builder: Building new path...");
        String target_path = GeneralDataAccess.getPath();
        String target_extension = getExtension(fileType);
        String target_file_name = GeneralText.getDialog().input("file_name");
        target_path += "/" +target_file_name + target_extension;
        logger.info("New path builder: Finished building new path: {}", target_path);
        return target_path;
    }

    public void setDatabaseInfo(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        logger.info("Setting DataSource...");
        this.dataSource = dataSource;
        logger.info("DataSource is set to: {}", dataSource);
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        logger.info("Setting FileType...");
        this.fileType = fileType;
        logger.info("FileType is set to: {}", fileType);
    }

    public void createNewFile() throws FileManageException {
        try {
            logger.info("Create new file: Creating new file...");
            if(new File(file_path).createNewFile()){
                GeneralText.getDialog().popup("file_created");
                logger.info("Create new file: New file has been created!");
            }else{
                logger.info("Create new file: File already exists, no file will be created");
            }
        } catch (IOException e) {
            throw new FileManageException(e.getMessage());
        }
    }

}
