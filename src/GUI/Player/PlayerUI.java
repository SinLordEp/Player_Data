package GUI.Player;

import GUI.GeneralText;
import Interface.EventListener;
import Interface.GeneralUI;
import control.PlayerControl;
import model.Player;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.SortedMap;
import java.util.TreeMap;

public class PlayerUI implements GeneralUI, EventListener<SortedMap<?,?>> {
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
    private PlayerTableModel tableModel;
    private int selected_player_id;

    public PlayerUI(PlayerControl control) {
        playerControl = control;
        this.playerControl.addListener(this);
    }

    private void initialize(){
        tableModel = new PlayerTableModel(new TreeMap<>());
        table_data.setModel(tableModel);
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroll_data.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setUIText();
        searchListener();
        buttonListener();
        tableListener();
    }

    @Override
    public void run() {
        initialize();
        JFrame frame = new JFrame(PlayerText.getDialog().getText("frame_title"));
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

    @SuppressWarnings("unchecked")
    private void refresh(Object object) {
        TreeMap<Integer, Player> playerMap = (TreeMap<Integer, Player>) object;
        tableModel.update_data(playerMap);
        table_data.setModel(tableModel);
    }

    private void setUIText(){
        button_add.setText(PlayerText.getDialog().getText("button_add"));
        button_modify.setText(PlayerText.getDialog().getText("button_modify"));
        button_delete.setText(PlayerText.getDialog().getText("button_delete"));
        button_export.setText(PlayerText.getDialog().getText("button_export"));
        button_import.setText(PlayerText.getDialog().getText("button_import"));
        button_createFile.setText(PlayerText.getDialog().getText("button_createFile"));
        button_language.setText(PlayerText.getDialog().getText("button_language"));
        label_search.setText(PlayerText.getDialog().getText("label_search"));
    }

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
        button_add.addActionListener(_ -> {
            button_add.setEnabled(false);
            playerControl.add();
            button_add.setEnabled(true);
        });

        button_modify.addActionListener(_ -> {
            button_modify.setEnabled(false);
            playerControl.modify(selected_player_id);
            button_modify.setEnabled(true);
        });

        button_delete.addActionListener(_ -> {
            button_delete.setEnabled(false);
            playerControl.delete(selected_player_id);
            button_delete.setEnabled(true);
        });

        button_export.addActionListener(_ -> {
            button_export.setEnabled(false);
            playerControl.export();
            button_export.setEnabled(true);
        });

        button_createFile.addActionListener(_ -> {
            button_createFile.setEnabled(false);
            playerControl.createFile();
            button_createFile.setEnabled(true);
        });

        button_import.addActionListener(_ -> {
            button_import.setEnabled(false);
            playerControl.importData();
            button_import.setEnabled(true);
        });

        button_language.addActionListener(_ -> {
            button_language.setEnabled(false);
            playerControl.changeLanguage();
            button_language.setEnabled(true);
        });
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
        setUIText();
        tableModel.language_changed();
    }

    private void dataSourceIsSet(){
        button_add.setEnabled(true);
    }

    @Override
    public void onEvent(String event, SortedMap<?,?> data) {
        switch(event){
            case "operation_cancelled", "db_login_failed", "db_login_cancelled", "config_error", "export_failed", "data_saved", "unknown_error" -> generalPopup(event);
            case "region_server_null", "player_map_null", "modified_player", "added_player", "deleted_player", "exported_file", "exported_db" -> playerPopup(event);
            case "data_changed"-> refresh(data);
            case "language_changed"-> changeLanguage();
            case "dataSource_set" -> dataSourceIsSet();
        }
    }

    private void generalPopup(String sub_type){
        GeneralText.getDialog().popup(sub_type);
    }

    private void playerPopup(String sub_type){
        PlayerText.getDialog().popup(sub_type);
    }
}


