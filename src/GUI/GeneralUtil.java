package GUI;

import main.OperationException;

import javax.swing.*;

public class GeneralUtil {

    public static String extension_general() {
        String[] options = {"DAT", "XML", "TXT"};
        return buildSelectionDialog("Extension selector","Choose a File type", options);
    }

    public static String buildSelectionDialog(String title, String message, String[] options) {
        int choice = JOptionPane.showOptionDialog(
                null,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == -1) throw new OperationException("Selection is invalid\n");
        return options[choice];
    }

    public static String input(String message){
        return JOptionPane.showInputDialog(message);
    }

    public static void popup(String message){
        JOptionPane.showMessageDialog(null, message);
    }

}
