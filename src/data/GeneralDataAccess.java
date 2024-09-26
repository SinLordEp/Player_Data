package data;

import DB.GeneralDBA;
import GUI.GeneralMenu;
import file.FileDataReader;
import file.FileDataWriter;
import main.OperationCanceledException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;


public abstract class GeneralDataAccess<T,K,V> {
    protected boolean file_changed = false;
    protected String file_path = null;
    protected FileDataReader<T> reader = null;
    protected FileDataWriter<T> writer = null;
    protected GeneralDBA DBAccess = null;
    protected boolean isDBSource = false;

    abstract void read() throws Exception;
    abstract void write() throws Exception;
    abstract void refresh() throws Exception;
    abstract void export() throws Exception;
    abstract String delete(V id) throws Exception;
    abstract String add(K data) throws Exception;
    abstract K pop(V id) throws Exception;
    abstract String update(K data) throws Exception;

    public String getFile_path() {
        return file_path;
    }

    public void setFile_path(String file_path) {
        this.file_path = file_path;
        setFile_changed(true);
    }

    public boolean isFile_changed() {
        return file_changed;
    }

    public void setFile_changed(boolean file_changed) {
        this.file_changed = file_changed;
    }

    public static String choose_extension(){
        return switch (GeneralMenu.extension_general()) {
            case "Binary DAT File" -> ".dat";
            case "XML File" -> ".xml";
            case "TXT File" -> ".txt";
            default -> throw new IllegalArgumentException("Unknown extension!!!");
        };
    }

    public static String get_path(String file_extension){
        JFileChooser fileChooser = new JFileChooser(new File("./src/config").getAbsolutePath());
        fileChooser.setDialogTitle("Choosing " + file_extension);
        switch(file_extension){
            case ".xml":
                fileChooser.setFileFilter(new FileNameExtensionFilter(".xml", "xml"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            case ".dat":
                fileChooser.setFileFilter(new FileNameExtensionFilter(".dat", "dat"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            case "path":
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);break;
        }
        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return fileChooser.getSelectedFile().getPath();
        throw new OperationCanceledException();
    }

    public static String new_path_builder(){
        String target_path = GeneralDataAccess.get_path("path");
        String target_extension = choose_extension();
        String target_file_name = GeneralMenu.universalInput("Input a file name");
        target_path += "/" +target_file_name + target_extension;
        return target_path;
    }

}
