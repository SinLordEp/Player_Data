package GUI.Player;

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
public class PlayerModify extends JDialog {
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
    private boolean ok = false;
    Set<Integer> playerIDs;

    //For modifying
    public PlayerModify(HashMap<String, String[]> regionServerMap, Player player) {
        this.player = player;
        this.regionServerMap = regionServerMap;
        initialize();
        setVisible(true);
    }
    //For adding
    public PlayerModify(HashMap<String, String[]> regionServerMap, Set<Integer> playerIDs, Player player) {
        this.player = player;
        this.regionServerMap = regionServerMap;
        this.playerIDs = playerIDs;
        initialize();
        setVisible(true);
    }

    private void initialize(){
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        setResizable(false);
        setLocationRelativeTo(null);
        setUIText();
        configureRegion();
        comboBoxListener();
        textListener();
        parsePlayer();
        pack();
        button_submit.addActionListener(_ -> onOK());
        button_cancel.addActionListener(_ -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void onOK() {
        player.setRegion(region);
        player.setServer(server);
        player.setName(text_name.getText());
        if(text_id.isVisible()){
            player.setID(Integer.parseInt(text_id.getText()));
        }
        ok = true;
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    private void setUIText(){
        label_id.setText("ID: ");
        label_region.setText(PlayerDialog.getDialog().getText("label_region"));
        label_server.setText(PlayerDialog.getDialog().getText("label_server"));
        label_name.setText(PlayerDialog.getDialog().getText("label_name"));
        button_submit.setText(PlayerDialog.getDialog().getText("button_submit"));
        button_cancel.setText(PlayerDialog.getDialog().getText("button_cancel"));
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

    private void textListener(){
        text_name.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                nameCheck();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                nameCheck();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                nameCheck();
            }
        });
    }

    private void idListener(){
        text_id.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                idCheck();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                idCheck();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                idCheck();
            }
        });
    }

    private void parsePlayer(){
        if(player.getID() == 0){
            label_id.setVisible(true);
            text_id.setVisible(true);
            idListener();
        }else{
            text_id.setVisible(false);
            label_id.setVisible(false);
            panel_info.setBorder(BorderFactory.createTitledBorder("ID: " + player.getID()));
            comboBox_region.setSelectedItem(player.getRegion());
            comboBox_server.setSelectedItem(player.getServer());
            text_name.setText(player.getName());
        }
    }

    public boolean isCancelled() {
        return !ok;
    }

    private void idCheck(){
        if(text_id.getText().isEmpty() || text_id.getText().isBlank()){
            label_error.setText("");
            return;
        }
        int id = Integer.parseInt(text_id.getText());
        if(playerIDs.contains(id)){
            label_error.setText(PlayerDialog.getDialog().getText("label_id_error"));
            label_error.setVisible(true);
            button_submit.setEnabled(false);
        }else{
            label_error.setText("");
            label_error.setVisible(false);
            button_submit.setEnabled(true);
        }
    }

    private void nameCheck(){
        String name = text_name.getText();
        button_submit.setEnabled(!name.isEmpty());
    }

    public Player getPlayer() {
        return player;
    }

}
