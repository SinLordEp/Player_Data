package GUI;

import exceptions.OperationCancelledException;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static main.principal.getProperty;


/**
 * The {@code GeneralText} class provides functionality for managing dialog configurations such as texts,
 * input prompts, popups, and options based on different languages and properties. It uses a singleton
 * pattern to ensure a single instance for managing the dialogs.
 * @author SIN
 */
public class TextHandler {
    private static final TextHandler GENERAL_TEXT = new TextHandler();
    private Map<String,Object> texts;
    private Map<String,Object> inputs;
    private Map<String,Object> popups;
    private Map<String,Object> options;
    private String language;

    public TextHandler() {
        URL resource = getClass().getResource(getProperty("playerDialog"));
        initialize(resource);
    }

    public static TextHandler fetch() {
        return GENERAL_TEXT;
    }

    /**
     * Initializes the dialog configurations by parsing a YAML file from the provided resource URL.
     * This method loads text, input, popup, and option definitions from the YAML resource
     * and sets them to the corresponding fields. It also sets the default language from
     * a system property.
     * <p>
     * If the provided {@code resource} is null, it throws an {@code IllegalArgumentException}.
     * If any I/O error occurs during resource reading, it displays an error message
     * using the {@code message} method.
     *
     * @param resource the URL of the configuration resource to parse and initialize the dialog definitions.
     *                 Must not be null. An {@code IllegalArgumentException} is thrown if null.
     */
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
        this.language = getProperty("defaultLanguage");
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Retrieves the localized text associated with a specific subtype and language.
     * This method returns the text for a given {@code sub_type} in the current language
     * provided that the {@code texts} map contains the subtype and its translations.
     * If the subtype is not found, it returns a formatted error message.
     *
     * @param sub_type the identifier for the text entry to retrieve; must match an existing key
     *                 in the {@code texts} map.
     * @return the text associated with the subtype in the current language, or an error
     *         message if the subtype is not found in the {@code texts} map.
     */
    @SuppressWarnings("unchecked")
    public String getText(String sub_type) {
        if(!texts.containsKey(sub_type)) {
            return "Sub_type: %s not found".formatted(sub_type);
        }
        return (String) ((Map<String, Object>)(texts.get(sub_type))).get(language);
    }

    /**
     * Retrieves the popup content associated with a specific subtype and the current language.
     * The method looks up the corresponding popup data for the provided {@code sub_type}
     * in the {@code popups} map. If a matching entry is found, it retrieves the value associated
     * with the current language. If the subtype does not exist in the {@code popups} map,
     * it returns a formatted error message.
     *
     * @param sub_type the identifier for the popup content to retrieve; must match an existing key
     *                 in the {@code popups} map.
     * @return the popup content for the specified subtype in the current language,
     *         or a formatted error message if the subtype is not found in the {@code popups} map.
     */
    @SuppressWarnings("unchecked")
    protected String getPopup(String sub_type) {
        if(!popups.containsKey(sub_type)) {
            return "Sub_type: %s not found".formatted(sub_type);
        }
        return (String) ((Map<String, Object>)(popups.get(sub_type))).get(language);
    }

    /**
     * Displays a popup dialog with content retrieved from the {@code getPopup} method
     * for the specified {@code sub_type}. The dialog is displayed using {@code JOptionPane}.
     * <p>
     * This method uses {@code getPopup(sub_type)} to fetch the localized content
     * based on the application's current language and the provided {@code sub_type}.
     * The popup functions as an informational message dialog.
     *
     * @param sub_type the identifier for the popup content to display. This value must
     *                 correspond to an existing key in the {@code popups} map.
     *                 If not found, a formatted error message is displayed.
     */
    public void popup(String sub_type) {
        JOptionPane.showMessageDialog(null, getPopup(sub_type));
    }

    /**
     * Displays an input dialog for the specified subtype, allowing the user to provide input.
     * If the provided subtype is not found in the {@code inputs} map, a default error prompt
     * is shown. Otherwise, the input dialog retrieves the corresponding localized message
     * based on the current language setting.
     *
     * @param sub_type the key identifying the input prompt to display. It must
     *                 correspond to an existing entry in the {@code inputs} map.
     *                 If not found, an error message is presented to the user.
     * @return the input provided by the user in the dialog, or {@code null} if the dialog is canceled.
     */
    @SuppressWarnings("unchecked")
    public String input(String sub_type) {
        if(!inputs.containsKey(sub_type)) {
            return JOptionPane.showInputDialog("Sub_type: %s not found".formatted(sub_type));
        }
        return JOptionPane.showInputDialog(((Map<String, Object>) inputs.get(sub_type)).get(language));
    }

    /**
     * Retrieves the option map associated with the specified subtype. If the subtype
     * is not present in the {@code options} map, it returns a map containing a single entry
     * with an error message indicating the subtype was not found and {@code null} as its value.
     *
     * @param sub_type the key identifying the option set to retrieve. It must match an
     *                 existing key in the {@code options} map.
     * @return the options associated with the specified subtype as a {@code Map<String, Object>},
     *         or a map with an error message if the subtype is not found in the {@code options} map.
     */
    @SuppressWarnings("unchecked")
    protected Map<String,Object> option(String sub_type){
        if(!options.containsKey(sub_type)) {
            Map<String,Object> null_options = new HashMap<>();
            null_options.put("Sub type: %s not found".formatted(sub_type), null);
            return null_options;
        }
        return (Map<String, Object>) options.get(sub_type);
    }

    /**
     * Displays a message dialog containing the provided message string.
     * This method uses {@code JOptionPane.showMessageDialog} to show the message
     * as an informational popup to the user. It does not allow user input and
     * is primarily used for displaying simple, non-interactive notifications.
     *
     * @param message the message string to display in the dialog. It must not be null.
     *                If the message is null, a {@code NullPointerException} may occur.
     */
    public void message(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    /**
     * Retrieves the title from the provided dialog map based on the current language setting.
     * This method accesses the "title" section of the {@code dialog} map, extracts a nested map,
     * and retrieves the corresponding title string for the current {@code language}.
     *
     * @param dialog the map containing dialog configuration data, where the "title" key maps
     *               to another nested map holding language-specific titles.
     * @return the title string in the current language, or {@code null} if the title
     *         or language-specific value is not found.
     */
    @SuppressWarnings("unchecked")
    protected String getTitle(Map<String, Object> dialog) {
        Map<String, Object> title_dialog = (Map<String, Object>) dialog.get("title");
        return (String) title_dialog.get(language);
    }

    /**
     * Retrieves the message from the provided dialog map based on the current language setting.
     * This method accesses the "message" section of the {@code dialog} map, extracts a nested map,
     * and retrieves the corresponding message string for the current {@code language}.
     *
     * @param dialog the map containing dialog configuration data, where the "message" key maps
     *               to another nested map holding language-specific messages. It must not be null.
     * @return the message string in the current language, or {@code null} if the message
     *         or language-specific value is not found in the provided map.
     */
    @SuppressWarnings("unchecked")
    protected String getMessage(Map<String, Object> dialog) {
        Map<String, Object> message_dialog = (Map<String, Object>) dialog.get("message");
        return (String) message_dialog.get(language);
    }

    /**
     * Retrieves the list of options associated with the provided {@code dialog} map
     * for the current language setting. The method accesses the "option" key in the
     * {@code dialog} map to find a nested map, and then retrieves the list of strings
     * corresponding to the current {@code language}. The list is converted to an array
     * and returned.
     *
     * @param dialog the map containing dialog configuration data, where the "option" key maps
     *               to another nested map holding language-specific options. It must not be null.
     *               If the "option" key or the current language-specific list is missing, a
     *               {@code NullPointerException} may occur.
     * @return an array of option strings for the current language, or an empty array if the list is empty.
     */
    @SuppressWarnings("unchecked")
    protected String[] getOptions(Map<String, Object> dialog) {
        Map<String, Object> options_dialog = (Map<String, Object>) dialog.get("option");
        return ((java.util.List<String>)options_dialog.get(language)).toArray(new String[0]);
    }

    /**
     * Retrieves the list of options associated with a specific subtype and the current language setting.
     * This method checks the {@code options} map for the specified subtype and attempts to retrieve
     * the associated list of options in the current language. If the subtype does not exist in the
     * {@code options} map, it returns an array containing a formatted error message indicating that
     * the subtype was not found.
     * <p>
     * The options are expected to be stored as a nested structure in the {@code options} map,
     * where the subtype maps to another map containing a key "option". The "option" key maps to a
     * language-specific list of options, which is then converted into an array and returned.
     *
     * @param sub_type the identifier for the options to retrieve. It must correspond to an
     *                 existing key in the {@code options} map. If the subtype does not exist,
     *                 an error message is returned as the only element in the array.
     * @return a string array containing the option list for the specified subtype and the
     *         current language, or an array containing a single error message if the subtype
     *         is not found.
     */
    @SuppressWarnings("unchecked")
    public String[] getOptions(String sub_type) {
        if(!options.containsKey(sub_type)) {
            String[] null_options = new String[1];
            null_options[0] = "Sub_type: %s not found".formatted(sub_type);
            return null_options;
        }
        return ((java.util.List<String>)(((Map<String, Object>) option(sub_type).get("option")).get(language))).toArray(new String[0]);
    }

    /**
     * Displays an option dialog to the user and allows selection from multiple options.
     * The dialog is configured based on the provided {@code sub_type}.
     * If the user closes the dialog without making a choice, an {@code OperationCancelledException} is thrown.
     *
     * @param sub_type a string representing the type or category of the dialog,
     *                 which is used to determine the configuration parameters by {@code option}.
     * @return the index of the selected option as an integer, where the index corresponds
     *         to the position of the selected option in the options array.
     * @throws OperationCancelledException if the dialog is closed without making a selection.
     */
    public int selectionDialog(String sub_type) {
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
        if (choice == -1) {
            throw new OperationCancelledException();
        }
        return choice;
    }

    /**
     * Displays a selection dialog to the user based on the specified subtype and options,
     * and returns the selected option.
     * <p>
     * The method creates a dialog using the specified subtype to determine the
     * dialog's properties such as title and message by calling the {@code option} method.
     * The {@code JOptionPane.showOptionDialog} method is used to display the dialog and capture
     * the user's choice. If the user cancels the operation, an {@code OperationCancelledException} is thrown.
     *
     * @param sub_type the subtype of the dialog, used to determine its message and title
     * @param options an array of options to be displayed in the dialog for selection
     * @return the selected option from the options array
     * @throws OperationCancelledException if the user closes the dialog without making a selection
     */
    public Object selectionDialog(String sub_type, Object[] options) {
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
        if (choice == -1) {
            throw new OperationCancelledException();
        }
        return options[choice];
    }

}
