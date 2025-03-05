package GUI.Player;

import GUI.TextHandler;
import GUI.LogStage;
import GUI.UiUtils;
import Interface.EventListener;
import Interface.GeneralUI;
import Interface.VerifiedEntity;
import control.PlayerControl;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * The {@code PlayerUI} class serves as the user interface to manage player-related operations.
 * It implements {@code GeneralUI} for general interface functions and {@code EventListener<SortedMap<?,?>>}
 * to handle events tied to player data.
 * <p>
 * This class provides UI-related functionalities such as displaying a table of players,
 * handling user interactions via buttons, and responding to application events.
 * <p>
 * It connects to player-specific logic through {@code PlayerControl} to execute commands
 * such as adding, modifying, exporting, or deleting player data.
 */
public class PlayerUI implements GeneralUI, EventListener<TreeMap<Integer, VerifiedEntity>> {
    private final PlayerControl playerControl;
    private JTable table_data;

    private JButton button_add;
    private JButton button_modify;
    private JButton button_delete;
    private JButton button_export;
    private JButton button_import;
    private JButton button_createFile;
    private JButton button_language;
    private JLabel label_search;
    private JTextField field_search;

    private JPanel main_panel;
    private JScrollPane scroll_data;
    private JTextPane textPane_log;
    private JButton button_search;
    private PlayerTableModel tableModel;
    private int selected_player_id;

    private final HashMap<String, Consumer<TreeMap<Integer, VerifiedEntity>>> eventWithMapHandler = new HashMap<>();
    private final HashMap<String, Runnable> eventWithoutDataHandler = new HashMap<>();
    private final StyledDocument log_document;

    public PlayerUI(PlayerControl control) {
        playerControl = control;
        log_document = textPane_log.getStyledDocument();
    }

    private void initialize(){
        tableModel = new PlayerTableModel(new TreeMap<>());
        table_data.setModel(tableModel);
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroll_data.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        UiUtils.setLabelButtonText(TextHandler.fetch(), main_panel);
        searchListener();
        buttonListener();
        tableListener();
        initializeEvent();
    }

    /**
     * Launches the player user interface by initializing its components
     * and displaying the main application window. This method sets up the
     * {@code JFrame} with a specified title, content pane, size, and
     * behavior.
     * <p>
     * The method performs the following operations:
     * - Invokes {@code initialize()} to set up the application's internal data
     *   structures and user interface elements.
     * - Creates and configures a {@code JFrame} instance with a localized title
     *   obtained from {@code GeneralText.fetch().getText("frame_title")}.
     * - Sets the main content panel to {@code main_panel}.
     * - Adjusts the window size, disables resizing, and centers the window on the screen.
     * - Makes the window visible and adds a {@code WindowListener} to manage
     *   user-triggered window-closing events.
     * <p>
     * A {@code WindowListener} is attached to the frame to handle the window closing event
     * using {@code playerControl.onWindowClosing()} as its callback. This ensures proper
     * cleanup and termination of the application when the user attempts to close the window.
     */
    @Override
    public void run() {
        initialize();
        JFrame frame = new JFrame(TextHandler.fetch().getText("frame_title"));
        frame.setContentPane(main_panel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                playerControl.onWindowClosing();
            }
        });
    }

    /**
     * Refreshes the table model and updates the table display with the provided player data.
     * This method processes the given {@code TreeMap} containing player information
     * and updates the associated table model by invoking {@code tableModel.update_data}.
     * The table view is refreshed by setting the updated model.
     *
     * @param playerMap an {@code Object} expected to be a {@code TreeMap<Integer, Player>}
     *               containing player IDs as keys and their corresponding {@code Player} objects as values.
     *               This map is used to update the table model data.
     */
    private void refresh(TreeMap<Integer, VerifiedEntity> playerMap) {
        tableModel.update_data(playerMap);
        onLog(LogStage.PASS, "refresh_pass");
    }

    /**
     * Adds a document listener to the search field that filters the table data
     * based on the search input. This method enhances the user interface by
     * providing a dynamic search mechanism for the table content.
     * <p>
     * When the text in the search field changes, the {@code SearchListener} is triggered
     * to execute its {@code update()} method. The method retrieves the search key
     * from the text field and scans the table model to find matching rows.
     * <p>
     * The search operates on the first column of the table model:
     * - If a match is found, the corresponding row is selected, and the table scrolls
     *   to bring the row into view using {@code table_data.scrollRectToVisible()}.
     * - If no match is found or the search input is empty, the table's selection is cleared.
     * <p>
     * This method ensures smooth user interaction for searching and locating entries
     * in the table.
     * <p>
     * Internally calls:
     * - {@code SearchListener.update()} for detecting changes in the search field.
     * - {@code tableModel.getRowCount()} to iterate through all rows.
     * - {@code tableModel.getValueAt(int rowIndex, int columnIndex)} to retrieve values
     *   from the first column for comparison.
     * - {@code table_data.setRowSelectionInterval(int index0, int index1)} to highlight
     *   matched rows in the table.
     * - {@code table_data.scrollRectToVisible(Rectangle aRect)} to scroll the view to the visible area of the selected row.
     * - {@code table_data.clearSelection()} to remove any row selection.
     */
    private void searchListener(){
        field_search.getDocument().addDocumentListener(new SearchListener() {
            @Override
            public void update() {
                String searchKey = field_search.getText();
                if (!searchKey.isEmpty()) {
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (tableModel.getValueAt(i, 0).toString().contains(searchKey)) {
                            table_data.setRowSelectionInterval(i, i);
                            table_data.scrollRectToVisible(table_data.getCellRect(i, 0, true));
                            break;
                        }else{
                            table_data.clearSelection();
                        }
                    }
                } else {
                    table_data.clearSelection();
                }
            }
        });
    }

    private void buttonListener(){
        button_add.addActionListener(_ -> playerControl.add());

        button_modify.addActionListener(_ -> playerControl.modify(selected_player_id));

        button_delete.addActionListener(_ -> playerControl.delete(selected_player_id));

        button_export.addActionListener(_ -> playerControl.export());

        button_createFile.addActionListener(_ -> playerControl.createFile());

        button_import.addActionListener(_ -> playerControl.importData());

        button_language.addActionListener(_ -> playerControl.changeLanguage());

        button_search.addActionListener(_ -> playerControl.search());
    }

    private void tableListener(){
        table_data.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table_data.getSelectedRow() != -1) {
                button_modify.setEnabled(true);
                button_delete.setEnabled(true);
                selected_player_id = (int) table_data.getValueAt(table_data.getSelectedRow(),0);
            } else {
                button_modify.setEnabled(false);
                button_delete.setEnabled(false);
                selected_player_id = -1;
            }
        });
    }

    private void changeLanguage(){
        UiUtils.setLabelButtonText(TextHandler.fetch(), main_panel);
        tableModel.language_changed();
    }

    private void dataSourceIsSet(){
        button_add.setEnabled(true);
    }

    private void initializeEvent(){
        eventWithMapHandler.put("data_changed", this::refresh);
        eventWithoutDataHandler.put("language_changed", this::changeLanguage);
        eventWithoutDataHandler.put("dataSource_set", this::dataSourceIsSet);
    }

    /**
     * Handles an event triggered by the application or its components. This callback method
     * processes specific event types and executes corresponding operations, such as displaying
     * popup messages, refreshing data, or changing application state.
     * <p>
     * Event types and their corresponding actions:
     * - General Events: Executes {@code generalPopup(String sub_type)} for handling events like
     *   "operation_cancelled", "db_login_failed", "db_login_cancelled", "config_error",
     *   "export_failed", "data_saved", "unknown_error", and "php_error".
     * - Player Events: Executes {@code playerPopup(String sub_type)} for player-related events
     *   such as "region_server_null", "player_map_null", "modified_player", "added_player",
     *   "deleted_player", "exported_file", "exported_db", and "exported_php".
     * - Data Change Event: Calls {@code refresh(Object object)} when "data_changed" event occurs
     *   to update and refresh the data in the UI.
     * - Language Change Event: Triggers {@code changeLanguage()} to update UI elements based on
     *   the current language settings when "language_changed" is detected.
     * - Data Source Set Event: Invokes {@code dataSourceIsSet()} when "dataSource_set" event is
     *   fired to update data source configuration within the application.
     *
     * @param event a {@code String} representing the type of event that occurred. Must be one
     *              of the supported predefined event types handled by this method.
     * @param data a {@code SortedMap<?,?>} containing additional data relevant to the event.
     *             This parameter is primarily used during the "data_changed" event to provide
     *             updated player data for refreshing the table view.
     */
    @Override
    public void onEvent(String event, TreeMap<Integer,VerifiedEntity> data) {
        if(eventWithMapHandler.containsKey(event)){
            eventWithMapHandler.get(event).accept(data);
        } else if (eventWithoutDataHandler.containsKey(event)) {
            eventWithoutDataHandler.get(event).run();
        } else{
            handleUnknownEvent(event);
        }
    }

    @Override
    public void onLog(LogStage stage, String... message) {
        SimpleAttributeSet set = new SimpleAttributeSet();
        Color color = switch (stage){
            case ONGOING -> Color.blue;
            case PASS -> Color.green;
            case FAIL -> Color.red;
            case ERROR -> Color.ORANGE;
            case INFO -> Color.darkGray;
        };
        StyleConstants.setForeground(set, color);
        try {
            if(message.length == 1){
                log_document.insertString(log_document.getLength(), TextHandler.fetch().getText(message[0]) + "\n", set);
            }else{
                log_document.insertString(log_document.getLength(), TextHandler.fetch().getText(message[0])+ message[1] + "\n", set);
            }
            textPane_log.setCaretPosition(log_document.getLength());
        } catch (BadLocationException e) {
            TextHandler.fetch().message("Failed to append log message with Cause: " + e.getMessage());
        }
    }

    private void handleUnknownEvent(String event){
        throw new RuntimeException("Unknown event " + event);
    }

}


