package GUI.Player;

import Interface.GeneralUI;
import control.PlayerControl;


import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.TreeMap;

public class PlayerUI implements GeneralUI {
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
        JFrame frame = new JFrame(PlayerDialog.getDialog().getText("frame_title"));
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

    @Override
    public void refresh() {
        tableModel.update_data(playerControl.getMap());
        table_data.setModel(tableModel);
    }

    public void setUIText(){
        button_add.setText(PlayerDialog.getDialog().getText("button_add"));
        button_modify.setText(PlayerDialog.getDialog().getText("button_modify"));
        button_delete.setText(PlayerDialog.getDialog().getText("button_delete"));
        button_export.setText(PlayerDialog.getDialog().getText("button_export"));
        button_import.setText(PlayerDialog.getDialog().getText("button_import"));
        button_createFile.setText(PlayerDialog.getDialog().getText("button_createFile"));
        button_language.setText(PlayerDialog.getDialog().getText("button_language"));
        label_search.setText(PlayerDialog.getDialog().getText("label_search"));
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
        button_add.addActionListener(_ -> playerControl.add());

        button_modify.addActionListener(_ -> playerControl.modify(selected_player_id));

        button_delete.addActionListener(_ -> playerControl.delete(selected_player_id));

        button_export.addActionListener(_ -> playerControl.export());

        button_createFile.addActionListener(_ -> playerControl.createFile());

        button_import.addActionListener(_ -> playerControl.importData());

        button_language.addActionListener(_ -> playerControl.changeLanguage());
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

    public void changeLanguage(){
        setUIText();
        tableModel.language_changed();
    }
}


