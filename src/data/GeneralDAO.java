package data;

import GUI.TextHandler;
import Interface.EntityParser;
import Interface.GeneralCRUD;
import Interface.VerifiedEntity;
import data.file.FileType;
import exceptions.ConfigErrorException;
import exceptions.FileManageException;
import exceptions.OperationCancelledException;
import exceptions.OperationException;
import model.DataInfo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.TreeMap;

import static main.principal.getProperty;


/**
 * This abstract class defines a generic data access interface for various
 * data-related operations, including reading, saving, and managing file paths,
 * database information, and file operations. It provides a framework to handle
 * different types of data sources and file types.
 * @author SIN
 */
public abstract class GeneralDAO {
    protected EntityParser entityParser;
    protected DataInfo dataInfo = new DataInfo();
    protected TreeMap<Integer, VerifiedEntity> dataContainer = new TreeMap<>();
    protected boolean isSaveToFileNeeded = false;
    protected abstract void isEntityValid(VerifiedEntity entity);
    public abstract DataInfo getDefaultDatabaseInfo(DataInfo dataInfo) throws ConfigErrorException;

    public GeneralDAO(EntityParser entityParser) {
        this.entityParser = entityParser;
    }

    public void findById(){
        int id = Integer.parseInt(JOptionPane.showInputDialog(null, TextHandler.fetch().getText("input_id_ongoing")));
        dataContainer.clear();
        dataContainer.put(id, null);
        CRUDFactory.getCRUD(dataInfo)
                .prepare()
                .read(entityParser.parseAll(dataInfo.getDataType()), DataOperation.SEARCH, dataContainer)
                .release();
        if(dataContainer.get(id) != null){
            validateAllData();
        }else{
            throw new OperationException("ID not found");
        }
    }

    public void findAll() {
        dataContainer.clear();
        try {
            CRUDFactory.getCRUD(dataInfo)
                    .prepare()
                    .read(entityParser.parseAll(dataInfo.getDataType()), DataOperation.READ, dataContainer)
                    .release();
            if(dataContainer != null && !dataContainer.isEmpty()){
                validateAllData();
            }
        } catch (Exception e) {
            dataContainer = new TreeMap<>();
            dataInfo = new DataInfo();
            throw new OperationException(e.getMessage());
        }
    }

    public void saveAllToFile(){
        if (Objects.requireNonNull(dataInfo.getDataType()) instanceof FileType) {
            CRUDFactory.getCRUD(dataInfo)
                    .prepare()
                    .update(entityParser.serializeAll(dataInfo.getDataType()), null, dataContainer)
                    .release();
            isSaveToFileNeeded = false;
        }
    }

    public void update(DataOperation operation, VerifiedEntity verifiedEntity){
        if(!(dataInfo.getDataType() instanceof FileType)){
            CRUDFactory.getCRUD(dataInfo)
                    .prepare()
                    .update(entityParser.serializeOne(dataInfo.getDataType()), operation, verifiedEntity)
                    .release();
        }else{
            isSaveToFileNeeded = true;
        }
        switch (operation){
            case ADD, MODIFY -> dataContainer.put(verifiedEntity.getID(), verifiedEntity);
            case DELETE -> dataContainer.remove(verifiedEntity.getID());
        }

    }

    public void exportFile(DataInfo targetDataInfo) {
        String target_extension = getExtension((FileType) targetDataInfo.getDataType());
        String target_path = getPath();
        String target_name = TextHandler.fetch().input("new_file_name");
        target_path += "/" + target_name + target_extension;
        createNewFile(target_path);
        targetDataInfo.setUrl(target_path);
        CRUDFactory.getCRUD(targetDataInfo)
                .prepare()
                .update(entityParser.serializeAll(targetDataInfo.getDataType()), null, dataContainer)
                .release();
    }

    public void exportDB(DataInfo exportDataBaseInfo) {
        TreeMap<Integer, VerifiedEntity> target_container = new TreeMap<>();
        GeneralCRUD<DataInfo> currentCRUD = CRUDFactory.getCRUD(exportDataBaseInfo)
                .prepare()
                .read(entityParser.parseAll(exportDataBaseInfo.getDataType()),null, target_container);
        target_container.forEach((id, verified_entity) -> {
            if(!dataContainer.containsKey(id)){
                currentCRUD.update(entityParser.serializeOne(exportDataBaseInfo.getDataType()), DataOperation.DELETE, verified_entity);
            }
        });
        dataContainer.forEach((id, verified_entity) -> {
            if(target_container.containsKey(id)){
                currentCRUD.update(entityParser.serializeOne(exportDataBaseInfo.getDataType()),DataOperation.MODIFY, verified_entity);
            }else{
                currentCRUD.update(entityParser.serializeOne(exportDataBaseInfo.getDataType()), DataOperation.ADD, verified_entity);
            }
        });
        currentCRUD.release();
    }

    public boolean isEmpty(){
        return dataContainer.isEmpty();
    }

    public TreeMap<Integer, VerifiedEntity> getDataContainer() {
        return dataContainer;
    }

    private void validateAllData(){
        dataContainer.forEach((_, verifiedEntity) -> isEntityValid(verifiedEntity));
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
        JFileChooser fileChooser = new JFileChooser(new File(getProperty("defaultFilePath")).getAbsolutePath());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choosing " + fileType);
        switch(fileType){
            case TXT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(TextHandler.fetch().getText("extension_txt"), "txt"));
                break;
            case DAT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(TextHandler.fetch().getText("extension_dat"), "dat"));
                break;
            case XML:
                fileChooser.setFileFilter(new FileNameExtensionFilter(TextHandler.fetch().getText("extension_xml"), "xml"));
                break;
        }
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
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
        JFileChooser fileChooser = new JFileChooser(new File(getProperty("defaultFilePath")).getAbsolutePath());
        fileChooser.setDialogTitle("Choosing path");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
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
        String target_path = GeneralDAO.getPath();
        String target_extension = getExtension(fileType);
        String target_file_name = TextHandler.fetch().input("file_name");
        target_path += "/" +target_file_name + target_extension;
        return target_path;
    }

    public void setDataInfo(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    public DataInfo getDataInfo() {
        return dataInfo;
    }

    /**
     * Creates a new file at the specified file path. If the file already exists, no file will be created.
     * Logs information about the operation's success or failure.
     * <p>
     * This method interacts with {@code GeneralText.fetch()} to display a popup informing the user of a successful
     * file creation. If an {@code IOException} occurs during the file creation process, the method wraps and throws it
     * as a {@code FileManageException} with the original exception's message.
     * <p>
     * The logger records the different states of the file operation, including whether the file was created,
     * already existed, or if an exception occurred.
     *
     * @throws FileManageException if an {@code IOException} occurs during file creation.
     */
    public void createNewFile(String file_path) throws FileManageException {
        try {
            if(!new File(file_path).createNewFile()){
                throw new FileManageException("Cannot create new file");
            }
        } catch (IOException e) {
            throw new FileManageException(e.getMessage());
        }
    }

    public void clearData(){
        dataContainer.clear();
    }

}
