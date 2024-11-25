package GUI;

import exceptions.OperationCancelledException;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import static main.principal.getProperty;


public class GeneralDialog {
    private static GeneralDialog instance;
    protected Map<String,Object> texts;
    protected Map<String,Object> inputs;
    protected Map<String,Object> popups;
    protected Map<String,Object> options;
    protected String language;

    public GeneralDialog() {
        URL resource = getClass().getResource(getProperty("generalDialog"));
        initialize(resource);
    }

    public static GeneralDialog getDialog() {
        if (instance == null) {
            instance = new GeneralDialog();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    protected void initialize(URL resource) {
        if(resource == null) {
            throw new IllegalArgumentException("GeneralDialog yaml config is null");
        }
        try(InputStream inputStream = resource.openStream()) {
            Yaml yaml = new Yaml();
            Map<String,Object> dialogs = yaml.load(inputStream);
            texts = (Map<String, Object>) dialogs.get("text");
            inputs = (Map<String, Object>) dialogs.get("input");
            popups = (Map<String, Object>) dialogs.get("popup");
            options = (Map<String, Object>) dialogs.get("options");
        }catch (IOException e){
            message("Initializing dialogs failed\n"+e.getMessage());
        }
        this.language = "en";
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @SuppressWarnings("unchecked")
    public String getText(String sub_type) {
        return (String) ((Map<String, Object>)(texts.get(sub_type))).get(language);
    }

    @SuppressWarnings("unchecked")
    public String getPopup(String sub_type) {
        return (String) ((Map<String, Object>)(popups.get(sub_type))).get(language);
    }

    @SuppressWarnings("unchecked")
    public void popup(String sub_type) {
        JOptionPane.showMessageDialog(null,((Map<String, Object>) popups.get(sub_type)).get(language));
    }

    @SuppressWarnings("unchecked")
    public String input(String sub_type) {
        return JOptionPane.showInputDialog(((Map<String, Object>) inputs.get(sub_type)).get(language));
    }

    @SuppressWarnings("unchecked")
    protected Map<String,Object> option(String sub_type){
        return (Map<String, Object>) options.get(sub_type);
    }

    public void message(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    @SuppressWarnings("unchecked")
    protected String getTitle(Map<String, Object> dialog) {
        Map<String, Object> title_dialog = (Map<String, Object>) dialog.get("title");
        return (String) title_dialog.get(language);
    }

    @SuppressWarnings("unchecked")
    protected String getMessage(Map<String, Object> dialog) {
        Map<String, Object> message_dialog = (Map<String, Object>) dialog.get("message");
        return (String) message_dialog.get(language);
    }

    @SuppressWarnings("unchecked")
    protected String[] getOptions(Map<String, Object> dialog) {
        Map<String, Object> options_dialog = (Map<String, Object>) dialog.get("option");
        return ((java.util.List<String>)options_dialog.get(language)).toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    public String[] getOptions(String sub_type) {
        return ((java.util.List<String>)(((Map<String, Object>) option(sub_type).get("option")).get(language))).toArray(new String[0]);
    }

    public int selectionDialog(String sub_type) throws OperationCancelledException {
        Map<String, Object> dialog = option(sub_type);
        String[] options = getOptions(dialog);
        int choice = JOptionPane.showOptionDialog(
                null,
                getMessage(dialog),
                getTitle(dialog),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == -1) throw new OperationCancelledException();
        return choice;
    }

    public Object selectionDialog(String sub_type, Object[] options) throws OperationCancelledException {
        Map<String, Object> dialog = option(sub_type);
        int choice = JOptionPane.showOptionDialog(
                null,
                getMessage(dialog),
                getTitle(dialog),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == -1) throw new OperationCancelledException();
        return options[choice];
    }

}
