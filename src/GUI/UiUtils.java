package GUI;

import javax.swing.*;
import java.awt.*;

/**
 * @author SIN
 */
public class UiUtils {
    public static void setLabelButtonText(GeneralText generalText, JPanel... panels) {
        for (JPanel panel : panels) {
            for(Component component : panel.getComponents()){
                if(component instanceof JLabel){
                    ((JLabel) component).setText(generalText.getText(component.getName()));
                }
                if(component instanceof JButton){
                    ((JButton) component).setText(generalText.getText(component.getName()));
                }
            }
        }
    }
}
