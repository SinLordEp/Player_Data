package utils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class File_Chooser {
    public static File get_path(String type) throws Exception {
        JFileChooser fileChooser = new JFileChooser(new File("./src/main").getAbsolutePath());
        fileChooser.setDialogTitle("Elegir la carpeta o el fichero");
        switch(type){
            case "xml":
                fileChooser.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            case "dat":
                fileChooser.setFileFilter(new FileNameExtensionFilter("DAT", "dat"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                break;
            case "path":
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);break;
        }

        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) return fileChooser.getSelectedFile();
        throw new Exception("Operation canceled");
    }
}
