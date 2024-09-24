package GUI;

import javax.swing.*;

public class GUI_utils {
    public static int buildSelectionDialog(String title, String[] options) {
        StringBuilder dialog = new StringBuilder(title);
        int i = 1;
        for (String option : options) {
            dialog.append("\n").append(i++).append(". ").append(option);
        }
        dialog.append("\n0. Exit");

        while (true) {
            try {
                String option_string = JOptionPane.showInputDialog(dialog);
                if(option_string == null){
                    return -1;
                }
                if(option_string.equals("0")) System.exit(0);

                int option_int = Integer.parseInt(option_string);
                if (option_int > 0 && option_int <= options.length) {
                    return option_int;
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Option invali");
            }
        }
    }
}
