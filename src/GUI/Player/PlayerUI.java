package GUI.Player;

import GUI.GeneralUI;
import control.PlayerControl;
import data.PlayerDataAccess;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;


public class PlayerUI implements GeneralUI {
    private final PlayerControl playerControl;
    private final JFrame frame;
    private JTable table_data;
    private JButton button_add;
    private JButton button_modify;
    private JButton Button_export;
    private JScrollPane Scroll_data;
    private JTextField field_search;
    private JLabel Label_search;
    private JPanel main_panel;
    private JButton button_delete;
    private final PlayerTableModel tableModel;
    private final PlayerDataAccess playerDA;
    private int selected_player_id;

    public PlayerUI(PlayerControl control) throws HeadlessException {
        frame = new JFrame("Player Menu");
        playerControl = control;
        playerDA = playerControl.getPlayerDA();
        TitledBorder border = BorderFactory.createTitledBorder(playerDA.isDBConnected());
        main_panel.setBorder(border);
        tableModel = new PlayerTableModel(playerDA.getPlayer_map());
        table_data.setModel(tableModel);
        button_add.addActionListener(_ -> {
            try {
                playerControl.create_player_control();
                refresh();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        button_modify.addActionListener(_ -> {
            try {
                playerControl.modify_player_control(selected_player_id);
                refresh();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        button_delete.addActionListener(_ -> {
            try {
                playerControl.delete_control(selected_player_id);
                refresh();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Button_export.addActionListener(_ -> {
            try {
                playerControl.export_control();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    @Override
    public void run() {
        frame.setContentPane(main_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        search_listener();
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //JTable listener
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

        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void close() {

    }

    @Override
    public void refresh() throws Exception {
        playerDA.refresh();
        tableModel.update_data(playerDA.getPlayer_map());
    }

    private void search_listener(){
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

}


