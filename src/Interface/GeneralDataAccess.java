package Interface;

import GUI.GeneralDialog;
import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;
import exceptions.ConfigErrorException;
import exceptions.OperationCancelledException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.HashMap;

import static main.principal.getProperty;


public abstract class GeneralDataAccess {
    protected String file_path = null;
    protected DataSource dataSource = DataSource.NONE;
    protected FileType fileType = FileType.NONE;
    public abstract HashMap<String, String> getDefaultDatabaseInfo(SqlDialect sqlDialect) throws ConfigErrorException;

    public void setFilePath(String file_path) {
        this.file_path = file_path;
        this.dataSource = DataSource.FILE;
    }

    public static String chooseExtension(){
        FileType[] fileTypes = FileType.values();
        FileType[] options = new FileType[fileTypes.length-1];
        System.arraycopy(fileTypes, 1, options, 0, fileTypes.length - 1);
        return switch (GeneralDialog.getDialog().selectionDialog("extension_general", options)) {
            case FileType.DAT -> ".dat";
            case FileType.XML -> ".xml";
            case FileType.TXT -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    public static String getPath(FileType fileType) throws OperationCancelledException {
        JFileChooser fileChooser = new JFileChooser(new File(getProperty("defaultFilePath")).getAbsolutePath());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choosing " + fileType);
        switch(fileType){
            case TXT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(GeneralDialog.getDialog().getText("extension_txt"), "txt"));
                break;
            case DAT:
                fileChooser.setFileFilter(new FileNameExtensionFilter(GeneralDialog.getDialog().getText("extension_dat"), "dat"));
                break;
            case XML:
                fileChooser.setFileFilter(new FileNameExtensionFilter(GeneralDialog.getDialog().getText("extension_xml"), "xml"));
                break;
        }
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getPath();
        }else {
            throw new OperationCancelledException();
        }

    }

    public static String getPath() throws OperationCancelledException {
        JFileChooser fileChooser = new JFileChooser(new File(getProperty("defaultFilePath")).getAbsolutePath());
        fileChooser.setDialogTitle("Choosing path");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getPath();
        }else{
            throw new OperationCancelledException();
        }
    }

    public static String newPathBuilder() throws OperationCancelledException {
        String target_path = GeneralDataAccess.getPath();
        String target_extension = chooseExtension();
        String target_file_name = GeneralDialog.getDialog().input("file_name");
        target_path += "/" +target_file_name + target_extension;
        return target_path;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
}
