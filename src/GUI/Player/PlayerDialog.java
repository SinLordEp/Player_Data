package GUI.Player;

import GUI.GeneralUtil;
import main.OperationException;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.util.Map;

public class PlayerDialog {
    private static PlayerDialog instance;
    private Map<String,Object> dialogs;
    private String language;

    private PlayerDialog() {
        initialize_dialogs();
        language = "en";
    }

    private void initialize_dialogs() {
        Yaml yaml = new Yaml();
        dialogs = yaml.load("src/GUI/Player/player_dialog.yaml");
    }

    public static PlayerDialog get() {
        if (instance == null) {
            instance = new PlayerDialog();
        }
        return instance;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> get_main(String main_type) {
        return (Map<String, Object>) dialogs.get(main_type);
    }

    @SuppressWarnings("unchecked")
    public String get_UI(String sub_type) {
        return (String) ((Map<String, Object>)(get_main("UI").get(sub_type))).get(sub_type);
    }

    @SuppressWarnings("unchecked")
    public void popup(String sub_type) {
        Map<String, String> sub_dialog = (Map<String, String>) get_main("popup").get(sub_type);
        GeneralUtil.popup(sub_dialog.get(language));
    }

    @SuppressWarnings("unchecked")
    public String input(String sub_type) {
        Map<String, String> sub_dialog = (Map<String, String>) get_main("input").get(sub_type);
        return GeneralUtil.input(sub_dialog.get(language));
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> options(String sub_type){
        Map<String, Object> dialog = (Map<String, Object>) dialogs.get("options");
        return (Map<String, Object>) dialog.get(sub_type);
    }

    public int modify_player(){
        return selectionDialog(options("modify_player"));
    }

    public String region_chooser(String[] region_list) {
        return selectionDialog(options("region_menu"),region_list);
    }

    public String server_chooser(String[] server_list){
        return selectionDialog(options("region_menu"),server_list);
    }

    public int export_player(){
        return selectionDialog(options("export_player"));
    }
    @SuppressWarnings("unchecked")
    private String get_title(Map<String, Object> dialog) {
        Map<String, Object> title_dialog = (Map<String, Object>) dialog.get("title");
        return (String) title_dialog.get(language);
    }

    @SuppressWarnings("unchecked")
    private String get_message(Map<String, Object> dialog) {
        Map<String, Object> message_dialog = (Map<String, Object>) dialog.get("message");
        return (String) message_dialog.get(language);
    }

    @SuppressWarnings("unchecked")
    private String[] get_options(Map<String, Object> dialog) {
        Map<String, Object> options_dialog = (Map<String, Object>) dialog.get("option");
        return ((java.util.List<String>)options_dialog.get(language)).toArray(new String[0]);
    }
    private int selectionDialog( Map<String, Object> dialog) {
        String[] options = get_options(dialog);
        int choice = JOptionPane.showOptionDialog(
                null,
                get_title(dialog),
                get_message(dialog),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == -1) throw new OperationException("Selection is invalid\n");
        return choice;
    }

    private String selectionDialog( Map<String, Object> dialog, String[] options) {
        int choice = JOptionPane.showOptionDialog(
                null,
                get_title(dialog),
                get_message(dialog),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == -1) throw new OperationException("Selection is invalid\n");
        return options[choice];
    }
}
