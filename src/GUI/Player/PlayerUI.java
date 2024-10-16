package GUI.Player;

import GUI.GeneralMenu;
import GUI.GeneralUI;
import control.PlayerControl;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.Arrays;
import java.util.TreeMap;


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
    private JButton button_importFile;
    private JButton button_importDB;
    private JButton button_createFile;
    private final PlayerTableModel tableModel;
    private int selected_player_id;

    public PlayerUI(PlayerControl control) {
        playerControl = control;
        frame = new JFrame("Player Menu");
        search_listener();
        button_listener();
        table_listener();
        db_initialize();
        TitledBorder border = BorderFactory.createTitledBorder("Data source: null");
        main_panel.setBorder(border);
        tableModel = new PlayerTableModel(new TreeMap<>());
        table_data.setModel(tableModel);
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    @Override
    public void run() {
        frame.setContentPane(main_panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    public void refresh() throws Exception {
        playerControl.refresh_DA();
        //tableModel = new PlayerTableModel(playerControl.getMap());
        tableModel.update_data(playerControl.getMap());
        table_data.setModel(tableModel);
        TitledBorder border = BorderFactory.createTitledBorder(playerControl.data_source());
        main_panel.setBorder(border);
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
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
        });

        button_modify.addActionListener(_ -> {
            try {
                playerControl.modify_player_control(selected_player_id);
                refresh();
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
        });

        button_delete.addActionListener(_ -> {
            try {
                playerControl.delete_control(selected_player_id);
                refresh();
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
        });

        button_export.addActionListener(_ -> {
            try {
                playerControl.export_control();
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
        });

        button_connectDB.addActionListener(_ -> {
            try {
                db_connect();
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
        });

        button_createFile.addActionListener(_ -> {
            try {
                playerControl.create_file();
                refresh();
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
        });

        button_importFile.addActionListener(_ -> {
            try {
                playerControl.import_file();
                refresh();
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
            button_importDB.setEnabled(true);
        });

        button_importDB.addActionListener(_ -> {
            playerControl.import_db();
            try {
                refresh();
            } catch (Exception e) {
                GeneralMenu.exception_message(e);
            }
            button_importDB.setEnabled(false);
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

    private void db_connect() throws Exception {
        switch(button_connectDB.getText()){
            case "Connect to DB":
                if(db_isBlank()){
                    GeneralMenu.message_popup("One or more Database field maybe empty");
                    return;
                }
                button_connectDB.setText("Connecting...");
                button_connectDB.setEnabled(false);
                char[] pwd = passwordField_pwd.getPassword();
                String password = new String(pwd);
                playerControl.configure_db(
                        text_URL.getText(),
                        text_database.getText(),
                        text_user.getText(),
                        password,
                        text_table.getText());
                if(playerControl.connect_db()){
                    lock_db_input();
                }else{
                    GeneralMenu.message_popup("Failed to connect to the database, please check the login info");
                    button_connectDB.setText("Connect to DB");
                    button_connectDB.setEnabled(true);
                }
                break;
            case "Disconnect":
                button_connectDB.setText("Disconnecting...");
                button_connectDB.setEnabled(false);
                if(playerControl.disconnect_db()){
                    unlock_db_input();
                }else{
                    GeneralMenu.message_popup("Some errors have occurred while disconnecting, please try again");
                    button_connectDB.setText("Disconnect");
                    button_connectDB.setEnabled(true);
                }
                if(playerControl.DB_source()){
                    tableModel.update_data(new TreeMap<>());
                    table_data.setModel(tableModel);
                }
                break;
        }
    }

    private void lock_db_input(){
        //when database connected
        label_DBStatus.setText("Database Connected");
        button_connectDB.setText("Disconnect");
        button_connectDB.setEnabled(true);
        button_importDB.setEnabled(true);
        text_URL.setEnabled(false);
        text_database.setEnabled(false);
        text_table.setEnabled(false);
        text_user.setEnabled(false);
        passwordField_pwd.setEnabled(false);
    }

    private void unlock_db_input(){
        button_connectDB.setText("Connect to DB");
        button_connectDB.setEnabled(true);
        text_URL.setEnabled(true);
        text_database.setEnabled(true);
        text_user.setEnabled(true);
        text_table.setEnabled(true);
        passwordField_pwd.setEnabled(true);
        button_importDB.setEnabled(false);
    }

}


