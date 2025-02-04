package GUI.Player;

import GUI.UiUtilities;
import Interface.CallBack;
import model.Player;
import model.Region;
import model.Server;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Set;

/**
 * The {@code PlayerInfoDialog} class represents a dialog window for inputting and modifying
 * player information such as region, server, name, and ID. This dialog is designed to ensure
 * validation and a user-friendly interface for creating or updating player data.
 * <p>
 * The dialog supports two modes of usage:
 * 1. Modifying an existing player.
 * 2. Adding a new player, along with ID validation.
 * @author SIN
 */
public class PlayerInfoDialog extends JDialog {
    private JPanel panel_main;
    private JButton button_submit;
    private JButton button_cancel;
    private JTextField text_name;
    private JComboBox<Region> comboBox_region;
    private JComboBox<Server> comboBox_server;
    private JLabel label_region;
    private JLabel label_server;
    private JLabel label_name;
    private JPanel panel_info;
    private JTextField text_id;
    private JLabel label_id;
    private JLabel label_id_error;
    private JPanel panel_button;
    private final Player player;
    private final HashMap<Region, Server[]> regionServerMap;
    private Region region;
    private Server server;
    Set<Integer> playerIDs;

    /**
     * Initializes and displays a dialog for managing and editing player information.
     * The dialog allows the user to view, modify, and submit player details along
     * with selecting a region and server. The submission and cancellation are
     * handled via the provided callback.
     *
     * @param regionServerMap A mapping of {@code Region} objects to arrays of {@code Server} objects,
     *                        representing the available regions and their corresponding servers.
     * @param player          The {@code Player} object whose information is being modified.
     * @param callBack        The {@code CallBack<Player>} instance used to handle submission
     *                        or cancellation actions. The {@code onSubmit(Player)} method is called
     *                        when submitting the dialog, and {@code onCancel()} is triggered when canceling.
     */
    public PlayerInfoDialog(HashMap<Region, Server[]> regionServerMap, Player player, CallBack<Player> callBack) {
        this.player = player;
        this.regionServerMap = regionServerMap;
        initialize(callBack);
        setVisible(true);
    }
    /**
     * Creates and displays a dialog window to add a new player's information. The dialog
     * allows the user to input player details such as the ID, name, region, and server.
     * The dialog also validates the player's inputs and uses the provided callback to
     * handle the submission or cancellation actions.
     *
     * @param regionServerMap A mapping of {@code Region} objects to arrays of {@code Server} objects,
     *                        representing the regions and their associated servers available for selection.
     * @param playerIDs       A set of existing player IDs to ensure that the entered ID does not conflict
     *                        with already registered players.
     * @param player          The {@code Player} instance to pre-fill certain fields if applicable. This may contain
     *                        null or default values if adding a new player.
     * @param callBack        The {@code CallBack<Player>} instance to handle the user's actions on the dialog.
     *                        The {@code onSubmit(Player)} method is called when the user submits valid
     *                        player information, while {@code onCancel()} is triggered if the dialog is canceled.
     */
    public PlayerInfoDialog(HashMap<Region, Server[]> regionServerMap, Set<Integer> playerIDs, Player player, CallBack<Player> callBack) {
        this.player = player;
        this.regionServerMap = regionServerMap;
        this.playerIDs = playerIDs;
        initialize(callBack);
        setVisible(true);
    }

    /**
     * Initializes the dialog with necessary UI components, listeners, and configurations
     * for handling player information. This method sets up the user interface, manages the
     * validation logic, and prepares the dialog for submission or cancellation actions.
     * The provided callback is used to handle the results of user actions.
     *
     * @param callBack the {@code CallBack<Player>} instance used to handle the results of the
     *                 dialog interactions. The {@code onSubmit(Player)} method is called when
     *                 the user submits valid data, and {@code onCancel()} is triggered if the dialog
     *                 is canceled.
     */
    private void initialize(CallBack<Player> callBack){
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        setResizable(false);
        setLocationRelativeTo(null);
        UiUtilities.setLabelButtonText(PlayerText.getDialog(), panel_info, panel_button);
        comboBoxListener();
        configureRegion();
        textValidateListener(text_name);
        parsePlayer();
        pack();
        button_submit.addActionListener(_ -> onOK(callBack));
        button_cancel.addActionListener(_ -> onCancel(callBack));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel(callBack);
            }
        });
    }

    /**
     * Handles the submission of player information in the dialog. Populates the {@code Player} object
     * with the entered field values, including region, server, name, and optionally ID if the ID field
     * is visible. Invokes the {@code onSubmit(Player)} method of the provided {@code CallBack<Player>}
     * instance if it is not null. Disposes the dialog after submission.
     *
     * @param callBack the {@code CallBack<Player>} instance used to handle the submission. The
     *                 {@code onSubmit(Player)} method is called with the updated {@code Player}
     *                 object if the callback is not null.
     */
    private void onOK(CallBack<Player> callBack) {
        player.setRegion(region);
        player.setServer(server);
        player.setName(text_name.getText());
        if(text_id.isVisible()){
            player.setID(Integer.parseInt(text_id.getText()));
        }
        if(callBack != null){
            callBack.onSubmit(player);
        }
        dispose();
    }

    /**
     * Handles the cancellation of the dialog.
     * Invokes the {@code onCancel()} method of the provided {@code CallBack<Player>} instance
     * to notify that the operation has been canceled and then disposes of the dialog
     * to release resources and close the interface.
     *
     * @param callBack the {@code CallBack<Player>} instance used to handle the cancellation action.
     *                 The {@code onCancel()} method is triggered to execute cancellation-related logic.
     */
    private void onCancel(CallBack<Player> callBack) {
        callBack.onCancel();
        dispose();
    }

    /**
     * Configures the combo box for region selection in the dialog.
     * <p>
     * This method populates the {@code comboBox_region} with keys from the {@code regionServerMap}.
     * Each key represents a {@code Region} that is available for selection in the dialog.
     * After adding all regions, it sets the selected item of {@code comboBox_region}
     * to the first region available in the map.
     * <p>
     * This method does not handle validation or modification of the {@code regionServerMap}.
     */
    private void configureRegion(){
        for (Region region : regionServerMap.keySet()){
            comboBox_region.addItem(region);
        }
        comboBox_region.setSelectedItem(regionServerMap.keySet().iterator().next());
    }

    /**
     * Configures the server combo box based on the selected region. This method retrieves
     * the list of servers associated with the specified {@code Region} from the {@code regionServerMap}
     * and populates the {@code comboBox_server} with the available servers.
     *
     * @param region the {@code Region} object whose associated servers are to be loaded
     *               into the {@code comboBox_server}.
     */
    private void configureServer(Region region){
        for(Server server: regionServerMap.get(region)){
            comboBox_server.addItem(server);
        }
    }

    /**
     * Configures item listeners for {@code comboBox_region} and {@code comboBox_server} to handle
     * user interactions and update related fields and components dynamically based on selection changes.
     * <p>
     * The listener for {@code comboBox_region} performs the following operations:
     * - Retrieves the selected {@code Region} object from {@code comboBox_region}.
     * - Updates the {@code region} field with the selected {@code Region}.
     * - Clears all items in {@code comboBox_server}.
     * - Invokes {@code configureServer(Region)} to populate {@code comboBox_server}
     *   with the servers associated with the selected region.
     * <p>
     * The listener for {@code comboBox_server} performs the following operation:
     * - Updates the {@code server} field with the selected {@code Server} object
     *   from {@code comboBox_server}.
     */
    private void comboBoxListener(){
        comboBox_region.addItemListener(_ ->{
            region = (Region) comboBox_region.getSelectedItem();
            comboBox_server.removeAllItems();
            configureServer(region);
        });
        comboBox_server.addItemListener(_ -> server = (Server) comboBox_server.getSelectedItem());
    }

    /**
     * Adds a {@code DocumentListener} to the text field {@code textName} to monitor changes
     * in its text content. This listener updates the state of the submit button
     * based on the validity of the entered data.
     * <p>
     * The listener performs the following checks and actions for each document update event:
     * - Calls {@code idCheck()}, {@code nameCheck()}, and {@code regionServerCheck()} to
     *   validate the ID, name, and region/server fields.
     * - Enables the submit button by calling {@code enableSubmitButton(boolean)} when
     *   all validation checks pass.
     * - Disables the submit button when validation fails.
     *
     * @param textName the {@code JTextField} instance whose text changes are monitored.
     */
    private void textValidateListener(JTextField textName) {
        textName.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                enableSubmitButton(idCheck() && nameCheck() && regionServerCheck());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                enableSubmitButton(idCheck() && regionServerCheck());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableSubmitButton(idCheck() && regionServerCheck());
            }
        });
    }

    /**
     * Parses and configures the dialog UI with the current player's details or prepares
     * the dialog for adding a new player based on the player's state.
     * <p>
     * If the player's ID is 0 (indicating a new player):
     * - Makes the ID input field ({@code text_id}) and its label ({@code label_id}) visible.
     * - Configures the server combo box for the default region using {@code configureServer(Region)}.
     * - Attaches a validation listener to {@code text_id} using {@code textValidateListener(JTextField)}.
     * <p>
     * If the player has an existing ID:
     * - Hides the ID input field ({@code text_id}) and its label ({@code label_id}).
     * - Updates the {@code panel_info} border to display the player's current ID.
     * - Sets the selected region in {@code comboBox_region} to the player's region.
     * - Configures the server combo box with the player's region using {@code configureServer(Region)}.
     * - Sets the selected server in {@code comboBox_server} to the player's server.
     * - Prefills the name input field ({@code text_name}) with the player's current name.
     */
    private void parsePlayer(){
        if(player.getID() == 0){
            label_id.setVisible(true);
            text_id.setVisible(true);
            label_id_error.setVisible(true);
            configureServer(comboBox_region.getItemAt(0));
            textValidateListener(text_id);
        }else{
            text_id.setVisible(false);
            label_id.setVisible(false);
            label_id_error.setVisible(false);
            panel_info.setBorder(BorderFactory.createTitledBorder("ID: " + player.getID()));
            comboBox_region.setSelectedItem(player.getRegion());
            configureServer(player.getRegion());
            comboBox_server.setSelectedItem(player.getServer());
            text_name.setText(player.getName());
        }
    }

    /**
     * Checks the validity of the entered player ID and returns whether it is valid.
     * This method verifies if the ID field is visible, contains valid input,
     * and ensures that the player ID does not already exist in the predefined list.
     * It updates the error label with appropriate messages if the input is invalid.
     *
     * @return {@code true} if the player ID is valid and does not exist in the list,
     *         otherwise {@code false}.
     */
    private boolean idCheck(){
        if(!text_id.isVisible()){
            return true;
        }
        if(text_id.getText().isEmpty() || text_id.getText().isBlank()){
            label_id_error.setText(" ");
            return false;
        }
        int id = Integer.parseInt(text_id.getText());
        if(playerIDs.contains(id)){
            label_id_error.setText(PlayerText.getDialog().getText("label_id_error"));
            return false;
        }else{
            label_id_error.setText(" ");
            return true;
        }
    }

    /**
     * Checks if the name provided in the text field is not empty.
     * <p>
     * This method retrieves the text from {@code text_name} using {@code getText()}.
     * It evaluates whether the retrieved text is empty or not.
     *
     * @return {@code true} if the name is not empty, {@code false} otherwise.
     */
    private boolean nameCheck(){
        String name = text_name.getText();
        return !name.isEmpty();
    }

    /**
     * Checks whether both the region and server combo boxes have selected items.
     * This method validates that the user has made a selection for both fields.
     *
     * @return {@code true} if both the region and server combo boxes have non-null selections,
     *         {@code false} otherwise.
     */
    private boolean regionServerCheck(){
        return comboBox_region.getSelectedItem() != null && comboBox_server.getSelectedItem() != null;
    }

    private void enableSubmitButton(boolean enabled){
        button_submit.setEnabled(enabled);
    }

    public Player getPlayer() {
        return player;
    }

}
