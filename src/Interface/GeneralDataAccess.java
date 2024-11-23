package Interface;

import GUI.GeneralDialog;
import data.DataSource;
import data.file.FileType;
import main.OperationException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.HashMap;


public abstract class GeneralDataAccess {
    protected String file_path = null;
    protected DataSource dataSource = DataSource.NONE;
    protected FileType fileType = FileType.NONE;
    public abstract HashMap<String, String> getDefaultDatabaseInfo();

    public void setFilePath(String file_path) {
        this.file_path = file_path;
        this.dataSource = DataSource.FILE;
    }

    public static String chooseExtension(){
        FileType[] fileTypes = FileType.values();
        String[] options = new String[fileTypes.length-1];
        for(int i = 1; i < fileTypes.length; i++){
            options[i-1] = fileTypes[i].toString();
        }
        return switch (GeneralDialog.getDialog().selectionDialog("extension_general", options)) {
            case "DAT" -> ".dat";
            case "XML" -> ".xml";
            case "TXT" -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    public static String getPath(FileType fileType){
        JFileChooser fileChooser = new JFileChooser(new File("./src/config").getAbsolutePath());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Choosing " + fileType);
        switch(fileType){
            case TXT:
                fileChooser.setFileFilter(new FileNameExtensionFilter("File source", "txt"));
                break;
            case DAT:
                fileChooser.setFileFilter(new FileNameExtensionFilter("File source", "dat"));
                break;
            case XML:
                fileChooser.setFileFilter(new FileNameExtensionFilter("File source", "xml"));
                break;
        }
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return fileChooser.getSelectedFile().getPath();
        throw new OperationException("Operation canceled\n");
    }

    public static String getPath(){
        JFileChooser fileChooser = new JFileChooser(new File("./src/config").getAbsolutePath());
        fileChooser.setDialogTitle("Choosing path");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return fileChooser.getSelectedFile().getPath();
        throw new OperationException("Operation canceled\n");
    }


    public static String newPathBuilder(){
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
