package GUI;

import main.OperationCanceledException;

import javax.swing.*;

public class GeneralMenu {

    public static String extension_general() {
        String[] options = {"Binary DAT File", "XML File", "TXT File"};
        return buildSelectionDialog("Extension selector","Choose a file type", options);
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
        if (choice == -1) throw new OperationCanceledException();
        return options[choice];
    }

    public static String universalInput(String message){
        return JOptionPane.showInputDialog(message);
    }

    public static void message_popup(String message){
        JOptionPane.showMessageDialog(null, message);
    }
}
