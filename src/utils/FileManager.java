package utils;

import model.GeneralOperationData;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileManager {

    public void readData(GeneralOperationData current_Data) throws Exception {
        if(current_Data.getReader() != null){
            current_Data.getReader().read(current_Data);
        }else{
            throw new IllegalStateException("Reader is not initialized");
        }
    }

    public void writeData(GeneralOperationData currentData) throws Exception {
        if(currentData.getWriter() != null){
            currentData.getWriter().write(currentData);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public void exportData(String file_extension,GeneralOperationData currentData) throws Exception {
        if(currentData.getWriter() != null){
            currentData.getWriter().export(file_extension, currentData);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }
    public static File create_file(String file_extension) throws Exception {
        JOptionPane.showMessageDialog(null,"Choosing file path");
        String path = get_path("path").getPath();
        String file_name = JOptionPane.showInputDialog("Input the file name");
        File data_file = new File(path + "/" + file_name + "." + file_extension);
        if(data_file.createNewFile()){
            return data_file;
        }else{
            throw new Exception("File already exist");
        }
    }

    public File read_file(String file_extension) throws Exception {
        File data_file = get_path(file_extension);
        if(data_file == null){
            throw new Exception("Operation canceled");
        }else{
            return data_file;
        }
    }

    public static File get_path(String file_extension) throws Exception {
        JFileChooser fileChooser = new JFileChooser(new File("./src/main").getAbsolutePath());
        fileChooser.setDialogTitle("Elegir la carpeta o el fichero");
        switch(file_extension){
            case "xml":
                fileChooser.setFileFilter(new FileNameExtensionFilter(".xml", "xml"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            case "dat":
                fileChooser.setFileFilter(new FileNameExtensionFilter(".dat", "dat"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            case "path":
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);break;
        }

        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return fileChooser.getSelectedFile();
        throw new Exception("Operation canceled");
    }
}
