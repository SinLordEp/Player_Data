package GUI;

import utils.OperationCanceledException;

import javax.swing.*;

public class GeneralMenu {

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
}
