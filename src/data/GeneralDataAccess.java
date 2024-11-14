package data;

import GUI.GeneralDialog;
import main.OperationException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;


public abstract class GeneralDataAccess {
    protected boolean data_changed = false;
    protected String file_path = null;
    protected boolean DB_source = false;

    abstract void read() throws Exception;
    abstract void write() throws Exception;

    public boolean isDBSource() {
        return DB_source;
    }

    public void setDBSource(boolean DB_source) {
        this.DB_source = DB_source;
        setDataChanged(true);
    }

    public void setFilePath(String file_path) {
        this.file_path = file_path;
        setDataChanged(true);
    }

    public boolean isDataChanged() {
        return data_changed;
    }

    public void setDataChanged(boolean data_changed) {
        this.data_changed = data_changed;
    }

    public static String chooseExtension(){
        String[] options = {"DAT", "XML", "TXT"};
        return switch (GeneralDialog.get().selectionDialog("extension_general", options)) {
            case "DAT" -> ".dat";
            case "XML" -> ".xml";
            case "TXT" -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    public String getFilePath() {
        return file_path;
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
        String target_file_name = GeneralDialog.get().input("file_name");
        target_path += "/" +target_file_name + target_extension;
        return target_path;
    }

}
