package GUI.Player;

import GUI.GeneralMenu;
import GUI.GeneralUI;
import control.PlayerControl;
import data.PlayerDataAccess;


import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.sql.SQLException;
import java.util.Arrays;


public class PlayerUI implements GeneralUI {
    private final PlayerControl playerControl;
    private final JFrame frame;
    private JTable table_data;
    private JButton button_add;
    private JButton button_modify;
    private JButton button_export;
    private JTextField field_search;
    private JPanel main_panel;
    private JButton button_delete;
    private JScrollPane Scroll_data;
    private JLabel label_search;
    private JTextField text_URL;
    private JTextField text_database;
    private JPasswordField passwordField_pwd;
    private JButton button_connectDB;
    private JTextField text_user;
    private JLabel label_DBStatus;
    private JLabel label_pwd;
    private JLabel label_user;
    private JLabel label_URL;
    private JLabel label_database;
    private JTextField text_table;
    private JLabel label_table;
    private PlayerTableModel tableModel;
    private final PlayerDataAccess playerDA;
    private int selected_player_id;

    public PlayerUI(PlayerControl control) {
        playerControl = control;
        frame = new JFrame("Player Menu");
        playerDA = playerControl.getPlayerDA();
        search_listener();
        button_listener();
        table_listener();
        db_initialize();
        TitledBorder border = BorderFactory.createTitledBorder(playerDA.dataSource());
        main_panel.setBorder(border);
    }

    @Override
    public void run() throws Exception {
        frame.setContentPane(main_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        if(playerDA.isDBOnly()){
            db_connect();
        }
        playerDA.refresh();
        tableModel = new PlayerTableModel(playerDA.getPlayer_map());
        table_data.setModel(tableModel);
        frame.setVisible(true);
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

    private void button_listener(){
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
        button_export.addActionListener(_ -> {
            try {
                playerControl.export_control();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        button_connectDB.addActionListener(_ -> {
            try {
                db_connect();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void table_listener(){
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

    private void db_initialize(){
        text_URL.setText("jdbc:mysql://localhost:3306");
        text_database.setText("person");
        text_table.setText("player");
        text_user.setText("root");
        passwordField_pwd.setText("root");
    }

    private boolean db_isBlank(){
        return (text_URL.getText().isBlank()) && (text_database.getText().isBlank()) && (text_table.getText().isBlank()) && (text_user.getText().isBlank()) && (Arrays.toString(passwordField_pwd.getPassword()).isBlank());
    }

    private void db_connect() throws SQLException {
        if(db_isBlank()){
            GeneralMenu.message_popup("One or more Database field maybe empty");
        }else{
            playerDA.getDBAccess().setURL(text_URL.getText());
            playerDA.getDBAccess().setDatabase(text_database.getText());
            playerDA.getDBAccess().setUser(text_user.getText());
            playerDA.getDBAccess().setTable(text_table.getText());
            char[] password = passwordField_pwd.getPassword();
            String password_str = new String(password);
            playerDA.getDBAccess().setPassword(password_str);
            playerDA.getDBAccess().connect();
        }
        if(playerDA.getDBAccess().connected()){
            label_DBStatus.setText("Database Connected");
            text_URL.setEnabled(false);
            text_database.setEnabled(false);
            text_table.setEnabled(false);
            text_user.setEnabled(false);
            passwordField_pwd.setEnabled(false);
            button_connectDB.setEnabled(false);
        }
    }


}


