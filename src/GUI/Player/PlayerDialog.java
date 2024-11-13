package GUI.Player;

import GUI.GeneralDialog;

public class PlayerDialog extends GeneralDialog {
    public static PlayerDialog instance;

    public static PlayerDialog get() {
        if (instance == null) {
            instance = new PlayerDialog();
        }
        return instance;
    }

    public int modify_player(){
        return selectionDialog(option("modify_player"));
    }

    public String region_chooser(String[] region_list) {
        return selectionDialog(option("region_menu"),region_list);
    }

    public String server_chooser(String[] server_list){
        return selectionDialog(option("region_menu"),server_list);
    }

    public int export_player(){
        return selectionDialog(option("export_player"));
    }

}
