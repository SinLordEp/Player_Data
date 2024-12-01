package GUI.Player;

import Interface.CallBack;
import model.Player;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Set;

/**
 * @author SIN
 */
public class PlayerInfoDialog extends JDialog {
    private JPanel panel_main;
    private JButton button_submit;
    private JButton button_cancel;
    private JTextField text_name;
    private JComboBox<String> comboBox_region;
    private JComboBox<String> comboBox_server;
    private JLabel label_region;
    private JLabel label_server;
    private JLabel label_name;
    private JPanel panel_info;
    private JTextField text_id;
    private JLabel label_id;
    private JLabel label_error;
    private final Player player;
    private final HashMap<String, String[]> regionServerMap;
    private String region, server;
    Set<Integer> playerIDs;

    //For modifying
    public PlayerInfoDialog(HashMap<String, String[]> regionServerMap, Player player, CallBack<Player> callBack) {
        this.player = player;
        this.regionServerMap = regionServerMap;
        initialize(callBack);
        setVisible(true);
    }
    //For adding
    public PlayerInfoDialog(HashMap<String, String[]> regionServerMap, Set<Integer> playerIDs, Player player, CallBack<Player> callBack) {
        this.player = player;
        this.regionServerMap = regionServerMap;
        this.playerIDs = playerIDs;
        initialize(callBack);
        setVisible(true);
    }

    private void initialize(CallBack<Player> callBack){
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        setResizable(false);
        setLocationRelativeTo(null);
        setUIText();
        configureRegion();
        comboBoxListener();
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

    private void onCancel(CallBack<Player> callBack) {
        callBack.onCancel();
        dispose();
    }

    private void setUIText(){
        label_id.setText("ID: ");
        label_region.setText(PlayerText.getDialog().getText("label_region"));
        label_server.setText(PlayerText.getDialog().getText("label_server"));
        label_name.setText(PlayerText.getDialog().getText("label_name"));
        button_submit.setText(PlayerText.getDialog().getText("button_submit"));
        button_cancel.setText(PlayerText.getDialog().getText("button_cancel"));
    }

    private void configureRegion(){
        for (String region : regionServerMap.keySet()){
            comboBox_region.addItem(region);
        }
    }

    private void configureServer(String region){
        for(String server: regionServerMap.get(region)){
            comboBox_server.addItem(server);
        }
    }

    private void comboBoxListener(){
        comboBox_region.addItemListener(_ ->{
            region = (String) comboBox_region.getSelectedItem();
            comboBox_server.removeAllItems();
            configureServer(region);
        });
        comboBox_server.addItemListener(_ -> server = (String) comboBox_server.getSelectedItem());
    }

    private void textValidateListener(JTextField textName) {
        textName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                enableSubmitButton(idCheck() && nameCheck() && serverCheck());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                enableSubmitButton(idCheck() && serverCheck());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                enableSubmitButton(idCheck() && serverCheck());
            }
        });
    }

    private void parsePlayer(){
        if(player.getID() == 0){
            label_id.setVisible(true);
            text_id.setVisible(true);
            configureServer(comboBox_region.getItemAt(0));
            textValidateListener(text_id);
        }else{
            text_id.setVisible(false);
            label_id.setVisible(false);
            panel_info.setBorder(BorderFactory.createTitledBorder("ID: " + player.getID()));
            comboBox_region.setSelectedItem(player.getRegion());
            comboBox_server.setSelectedItem(player.getServer());
            text_name.setText(player.getName());
        }
    }

    private boolean idCheck(){
        if(!text_id.isEnabled()){
            return true;
        }
        if(text_id.getText().isEmpty() || text_id.getText().isBlank()){
            label_error.setText("");
            return false;
        }
        int id = Integer.parseInt(text_id.getText());
        if(playerIDs.contains(id)){
            label_error.setText(PlayerText.getDialog().getText("label_id_error"));
            label_error.setVisible(true);
            return false;
        }else{
            label_error.setText("");
            label_error.setVisible(false);
            return true;
        }
    }

    private boolean nameCheck(){
        String name = text_name.getText();
        return !name.isEmpty();
    }

    private boolean serverCheck(){
        return comboBox_server.getSelectedItem() != null;
    }

    private void enableSubmitButton(boolean enabled){
        button_submit.setEnabled(enabled);
    }

    public Player getPlayer() {
        return player;
    }

}
