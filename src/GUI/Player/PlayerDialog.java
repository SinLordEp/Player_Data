package GUI.Player;

import GUI.GeneralDialog;

public class PlayerDialog extends GeneralDialog {
    private static PlayerDialog instance;

    public PlayerDialog() {
        super.initialize("src/GUI/Player/player_dialog.yaml");
    }

    public static PlayerDialog getDialog() {
        if (instance == null) {
            instance = new PlayerDialog();
        }
        return instance;
    }

}
