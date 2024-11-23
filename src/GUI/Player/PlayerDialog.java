package GUI.Player;

import GUI.GeneralDialog;

import java.net.URL;

import static main.principal.getProperty;

public class PlayerDialog extends GeneralDialog {
    private static PlayerDialog instance;

    public PlayerDialog() {
        URL resource = getClass().getResource(getProperty("playerDialog"));
        super.initialize(resource);
    }

    public static PlayerDialog getDialog() {
        if (instance == null) {
            instance = new PlayerDialog();
        }
        return instance;
    }

}
