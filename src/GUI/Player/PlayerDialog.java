package GUI.Player;

import GUI.GeneralUtil;
import main.OperationException;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class PlayerDialog {
    private static PlayerDialog instance;
    private Map<String,Object> texts;
    private Map<String,Object> inputs;
    private Map<String,Object> popups;
    private Map<String,Object> options;
    private String language;

    private PlayerDialog() {
        initialize_dialogs();
        language = "en";
    }

    @SuppressWarnings("unchecked")
    private void initialize_dialogs() {
        Yaml yaml = new Yaml();
        try(InputStream inputStream = new FileInputStream("src/GUI/Player/player_dialog.yaml")){
            Map<String,Object> dialogs = yaml.load(inputStream);
            texts = (Map<String, Object>) dialogs.get("text");
            inputs = (Map<String, Object>) dialogs.get("input");
            popups = (Map<String, Object>) dialogs.get("popup");
            options = (Map<String, Object>) dialogs.get("options");
        }catch (IOException e){
            throw new OperationException("Initializing dialogs failed\n"+e.getMessage());
        }
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
    public String get_text(String sub_type) {
        return (String) ((Map<String, Object>)(texts.get(sub_type))).get(language);
    }

    @SuppressWarnings("unchecked")
    public void popup(String sub_type) {
        GeneralUtil.popup((String) ((Map<String, Object>) popups.get(sub_type)).get(language));
    }

    @SuppressWarnings("unchecked")
    public String input(String sub_type) {
        return GeneralUtil.input((String) ((Map<String, Object>) inputs.get(sub_type)).get(language));
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> option(String sub_type){
        return (Map<String, Object>) options.get(sub_type);
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
