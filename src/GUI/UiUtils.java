package GUI;

import javax.swing.*;
import java.util.Arrays;

/**
 * @author SIN
 */
public class UiUtils {
    public static void setLabelButtonText(TextHandler textHandler, JPanel... panels) {
        Arrays.stream(panels).iterator().forEachRemaining(panel -> Arrays.stream(panel.getComponents()).iterator().forEachRemaining(component -> {
            if(component instanceof JLabel){
                ((JLabel) component).setText(textHandler.getText(component.getName()));
            }
            if(component instanceof JButton){
                ((JButton) component).setText(textHandler.getText(component.getName()));
            }
        }));
    }
}
