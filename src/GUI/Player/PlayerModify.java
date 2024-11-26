package GUI.Player;

import model.Player;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

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
    private final Player player;
    private final HashMap<String, String[]> regionServerMap;
    private String region, server;
    private boolean ok = false;

    public PlayerModify(HashMap<String, String[]> regionServerMap, Player player) {
        this.player = player;
        this.regionServerMap = regionServerMap;
        initialize();
        setVisible(true);
    }

    private void initialize(){
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        setResizable(false);
        setLocationRelativeTo(null);
        configureRegion();
        comboBoxListener();
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
        String name = text_name.getText();
        if (name == null || name.isEmpty()) {
            return;
        }
        player.setName(name);
        ok = true;
        dispose();
    }

    private void onCancel() {
        dispose();
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

    private void parsePlayer(){
        panel_info.setBorder(BorderFactory.createTitledBorder("ID: " + player.getID()));
        comboBox_region.setSelectedItem(player.getRegion());
        comboBox_server.setSelectedItem(player.getServer());
        text_name.setText(player.getName());
    }

    public boolean isOK() {
        return ok;
    }
}
