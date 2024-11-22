package Interface;

import GUI.GeneralDialog;
import data.DataSource;
import main.OperationException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.HashMap;


public abstract class GeneralDataAccess {
    protected String file_path = null;
    protected DataSource dataSource = DataSource.NONE;
    public abstract HashMap<String, String> getDefaultDatabaseInfo();

    public void setFilePath(String file_path) {
        this.file_path = file_path;
        this.dataSource = DataSource.FILE;
    }

    public static String chooseExtension(){
        String[] options = {"DAT", "XML", "TXT"};
        return switch (GeneralDialog.getDialog().selectionDialog("extension_general", options)) {
            case "DAT" -> ".dat";
            case "XML" -> ".xml";
            case "TXT" -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    public static String getPath(String path_or_file){
        JFileChooser fileChooser = new JFileChooser(new File("./src/config").getAbsolutePath());
        fileChooser.setDialogTitle("Choosing " + path_or_file);
        switch(path_or_file){
            case "path":
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);break;
            case "file":
                fileChooser.setFileFilter(new FileNameExtensionFilter("File source", "xml", "txt", "dat"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
        }
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return fileChooser.getSelectedFile().getPath();
        throw new OperationException("Operation canceled\n");
    }

    public static String newPathBuilder(){
        String target_path = GeneralDataAccess.getPath("path");
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


}
