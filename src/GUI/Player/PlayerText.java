package GUI.Player;

import GUI.GeneralText;

import java.net.URL;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class PlayerText extends GeneralText {
    private static PlayerText instance;

    public PlayerText() {
        URL resource = getClass().getResource(getProperty("playerDialog"));
        super.initialize(resource);
    }

    public static PlayerText getDialog() {
        if (instance == null) {
            instance = new PlayerText();
        }
        return instance;
    }

}
