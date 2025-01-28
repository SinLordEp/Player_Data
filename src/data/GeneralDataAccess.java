package data;

import GUI.Player.PlayerText;
import data.file.FileType;
import data.http.PhpType;
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
 * This abstract class defines a generic data access interface for various
 * data-related operations, including reading, saving, and managing file paths,
 * database information, and file operations. It provides a framework to handle
 * different types of data sources and file types.
 * @author SIN
 */
public abstract class GeneralDataAccess {
    protected String file_path = null;
    protected DataSource dataSource = DataSource.NONE;
    protected FileType fileType = FileType.NONE;
    protected PhpType phpType = PhpType.NONE;
    protected DatabaseInfo databaseInfo = new DatabaseInfo();
    public abstract void read();
    public abstract void save();
    private static final Logger logger = LoggerFactory.getLogger(GeneralDataAccess.class);

    /**
     * Sets the file path and updates the associated {@code dataSource} to {@code DataSource.FILE}.
     * Logs information about the updated file path and data source.
     *
     * @param file_path the file path to be set. It must be a valid string path for proper functionality.
     */
    public void setFilePath(String file_path) {
        this.file_path = file_path;
        logger.info("Set file path: Path is set to {}", file_path);
        this.dataSource = DataSource.FILE;
        logger.info("Set file path: DataSource is set to: File");
    }

    /**
     * Retrieves the file extension corresponding to the given {@code FileType}.
     * This method uses a {@code switch} construct to map specific {@code FileType} enum values
     * to their respective file extensions. If the provided {@code FileType} is not recognized,
     * the method throws an {@code IllegalArgumentException}.
     *
     * @param fileType the {@code FileType} enum value for which the file extension is required.
     *                 Must be one of the predefined constants in the {@code FileType} enum.
     * @return the file extension corresponding to the provided {@code FileType}.
     *         For example, ".txt" for {@code FileType.TXT}, ".xml" for {@code FileType.XML}.
     * @throws IllegalArgumentException if the provided {@code FileType} is not recognized.
     */
    public static String getExtension(FileType fileType) {
        logger.info("Get extension: Getting extension for {}", fileType);
        return switch (fileType) {
            case FileType.DAT -> ".dat";
            case FileType.XML -> ".xml";
            case FileType.TXT -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    /**
     * Retrieves the file path for a given {@code FileType} by displaying a file chooser dialog to the user.
     * The dialog filters files based on the provided {@code FileType} (e.g., TXT, DAT, XML).
     * If no selection is made, an {@code OperationCancelledException} is thrown.
     *
     * @param fileType the type of file to filter for in the file chooser. It must be one of the constants in {@code FileType}.
     * @return the absolute file path of the selected file as a {@code String}.
     * @throws OperationCancelledException if the user cancels the operation or does not select any file.
     */
    public static String getPath(FileType fileType) {
        logger.info("Get path: Getting path with file type paragram: {}", fileType);
        JFileChooser fileChooser = new JFileChooser(new File(getProperty("defaultFilePath")).getAbsolutePath());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choosing " + fileType);
        switch(fileType){
            case TXT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(PlayerText.getDialog().getText("extension_txt"), "txt"));
                break;
            case DAT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(PlayerText.getDialog().getText("extension_dat"), "dat"));
                break;
            case XML:
                fileChooser.setFileFilter(new FileNameExtensionFilter(PlayerText.getDialog().getText("extension_xml"), "xml"));
                break;
        }
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            logger.info("Get path: Finished get path with paragram!");
            return fileChooser.getSelectedFile().getPath();
        }else {
            throw new OperationCancelledException();
        }
    }

    /**
     * Displays a directory chooser dialog to the user and retrieves the selected directory's path.
     * The method uses {@code JFileChooser} to prompt the user for directory selection.
     * If the user cancels the operation, an {@code OperationCancelledException} is thrown.
     *
     * @return the absolute path of the selected directory as a {@code String}.
     * @throws OperationCancelledException if the user cancels the directory selection.
     */
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

    /**
     * Builds a new file path using the provided {@code FileType}.
     * This method interacts with {@code GeneralDataAccess.getPath()} to retrieve the target directory,
     * and {@code getExtension(FileType)} to determine the appropriate file extension for the given {@code FileType}.
     * Additionally, it uses a dialog to prompt the user to input the file name and appends the file name
     * with the extension to the target directory path. The constructed path is logged and then returned.
     *
     * @param fileType the {@code FileType} enum value representing the type of file to be created.
     *                 Must be one of the predefined constants in the {@code FileType} enum.
     * @return the fully constructed file path as a {@code String}, combining the directory path,
     *         user input file name, and file extension.
     */
    public static String newPathBuilder(FileType fileType) {
        logger.info("New path builder: Building new path...");
        String target_path = GeneralDataAccess.getPath();
        String target_extension = getExtension(fileType);
        String target_file_name = PlayerText.getDialog().input("file_name");
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

    public void setPhpType(PhpType phpType) {
        this.phpType = phpType;
    }

    /**
     * Creates a new file at the specified file path. If the file already exists, no file will be created.
     * Logs information about the operation's success or failure.
     * <p>
     * This method interacts with {@code PlayerText.getDialog()} to display a popup informing the user of a successful
     * file creation. If an {@code IOException} occurs during the file creation process, the method wraps and throws it
     * as a {@code FileManageException} with the original exception's message.
     * <p>
     * The logger records the different states of the file operation, including whether the file was created,
     * already existed, or if an exception occurred.
     *
     * @throws FileManageException if an {@code IOException} occurs during file creation.
     */
    public void createNewFile() throws FileManageException {
        try {
            logger.info("Create new file: Creating new file...");
            if(new File(file_path).createNewFile()){
                PlayerText.getDialog().popup("file_created");
                logger.info("Create new file: New file has been created!");
            }else{
                logger.info("Create new file: File already exists, no file will be created");
            }
        } catch (IOException e) {
            throw new FileManageException(e.getMessage());
        }
    }

}
