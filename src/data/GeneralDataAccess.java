package data;

import GUI.GeneralUtil;
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

    public boolean DB_source() {
        return DB_source;
    }

    public void setDB_source(boolean DB_source) {
        this.DB_source = DB_source;
        setData_changed(true);
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
        setData_changed(true);
    }

    public boolean isData_changed() {
        return data_changed;
    }

    public void setData_changed(boolean data_changed) {
        this.data_changed = data_changed;
    }

    public static String choose_extension(){
        return switch (GeneralUtil.extension_general()) {
            case "DAT" -> ".dat";
            case "XML" -> ".xml";
            case "TXT" -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    public String getFile_path() {
        return file_path;
    }

    public static String get_path(String path_or_file){
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

    public static String new_path_builder(){
        String target_path = GeneralDataAccess.get_path("path");
        String target_extension = choose_extension();
        String target_file_name = GeneralUtil.input("Input file name");
        target_path += "/" +target_file_name + target_extension;
        return target_path;
    }

}
