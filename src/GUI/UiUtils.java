package GUI;

import javax.swing.*;
import java.util.Arrays;

/**
 * @author SIN
 */
public class UiUtils {
    public static void setLabelButtonText(GeneralText generalText, JPanel... panels) {
        Arrays.stream(panels).iterator().forEachRemaining(panel -> Arrays.stream(panel.getComponents()).iterator().forEachRemaining(component -> {
            if(component instanceof JLabel){
                ((JLabel) component).setText(generalText.getText(component.getName()));
            }
            if(component instanceof JButton){
                ((JButton) component).setText(generalText.getText(component.getName()));
            }
        }));
    }
}
