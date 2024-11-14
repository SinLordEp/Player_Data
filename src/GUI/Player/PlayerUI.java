package GUI.Player;

import GUI.GeneralDialog;
import Interface.GeneralUI;
import control.PlayerControl;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeMap;


public class PlayerUI implements GeneralUI {
    private final PlayerControl playerControl;
    private final JFrame frame;
    private JTable table_data;

    private JButton button_add;
    private JButton button_modify;
    private JButton button_delete;
    private JButton button_export;
    private JButton button_connectDB;
    private JButton button_importFile;
    private JButton button_importDB;
    private JButton button_createFile;

    private JLabel label_port;
    private JLabel label_search;
    private JLabel label_pwd;
    private JLabel label_user;
    private JLabel label_URL;
    private JLabel label_database;

    private JTextField field_search;
    private JTextField text_URL;
    private JTextField text_database;
    private JTextField text_user;
    private JTextField text_port;

    private JPanel main_panel;
    private JScrollPane Scroll_data;
    private JPasswordField passwordField_pwd;
    private JComboBox<String> comboBox_SQL;
    private final PlayerTableModel tableModel;
    private int selected_player_id;
    private boolean db_connected;

    public PlayerUI(PlayerControl control) {
        playerControl = control;
        frame = new JFrame(PlayerDialog.get().get_text("frame_title"));
        TitledBorder border = BorderFactory.createTitledBorder(PlayerDialog.get().get_text("default_data_source"));
        main_panel.setBorder(border);
        tableModel = new PlayerTableModel(new TreeMap<>());
        table_data.setModel(tableModel);
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setUIText();
        db_initialize();
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
        tableModel.update_data(playerControl.getMap());
        table_data.setModel(tableModel);
        configure_title();
    }

    public void setUIText(){
        button_add.setText(PlayerDialog.get().get_text("button_add"));
        button_modify.setText(PlayerDialog.get().get_text("button_modify"));
        button_delete.setText(PlayerDialog.get().get_text("button_delete"));
        button_export.setText(PlayerDialog.get().get_text("button_export"));
        button_connectDB.setText(PlayerDialog.get().get_text("button_connectDB"));
        button_importFile.setText(PlayerDialog.get().get_text("button_importFile"));
        button_importDB.setText(PlayerDialog.get().get_text("button_importDB"));
        button_createFile.setText(PlayerDialog.get().get_text("button_createFile"));
        label_port.setText(PlayerDialog.get().get_text("label_port"));
        label_search.setText(PlayerDialog.get().get_text("label_search"));
        label_pwd.setText(PlayerDialog.get().get_text("label_pwd"));
        label_user.setText(PlayerDialog.get().get_text("label_user"));
        label_URL.setText(PlayerDialog.get().get_text("label_URL"));
        label_database.setText(PlayerDialog.get().get_text("label_database"));

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
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_modify.addActionListener(_ -> {
            try {
                playerControl.modify_player_control(selected_player_id);
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_delete.addActionListener(_ -> {
            try {
                playerControl.delete_control(selected_player_id);
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_export.addActionListener(_ -> {
            try {
                playerControl.export_control();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_connectDB.addActionListener(_ -> {
            try {
                db_connect();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_createFile.addActionListener(_ -> {
            try {
                playerControl.create_file();
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_importFile.addActionListener(_ -> {
            try {
                playerControl.import_file();
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
            button_importDB.setEnabled(true);
        });

        button_importDB.addActionListener(_ -> {
            playerControl.import_db();
            try {
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
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

    private void comboBox_listener(){
        comboBox_SQL.addActionListener(_ -> {
            switch((String) Objects.requireNonNull(comboBox_SQL.getSelectedItem())){
                case "MySQL":
                    label_URL.setText("jdbc:mysql://");
                    text_URL.setText("localhost");
                    text_database.setText("person");
                    text_database.setEnabled(true);
                    text_port.setText("3306");
                    text_port.setEnabled(true);
                    text_user.setText("root");
                    text_user.setEnabled(true);
                    passwordField_pwd.setText("root");
                    passwordField_pwd.setEnabled(true);
                    break;
                case "SQLite":
                    label_URL.setText("jdbc:sqlite:");
                    text_URL.setText("person.db");
                    text_database.setText("");
                    text_database.setEditable(false);
                    text_port.setText("");
                    text_port.setEditable(false);
                    text_user.setText("");
                    text_user.setEditable(false);
                    passwordField_pwd.setText("");
                    passwordField_pwd.setEditable(false);
                    break;
            }
        });
    }

    private void db_initialize(){
        comboBox_SQL.addItem("MySQL");
        comboBox_SQL.addItem("SQLite");
        search_listener();
        button_listener();
        table_listener();
        comboBox_listener();
        comboBox_SQL.setSelectedIndex(0);
        db_connected = false;
    }

    private boolean db_isBlank(){
        return (text_URL.getText().isBlank()) && (text_database.getText().isBlank()) && (text_port.getText().isBlank()) && (text_user.getText().isBlank()) && (Arrays.toString(passwordField_pwd.getPassword()).isBlank());
    }

    private void db_connect() {
        if (!db_connected) {
            if(db_isBlank()){
                GeneralDialog.get().popup("db_field_empty");
                return;
            }
            button_connectDB.setText(PlayerDialog.get().get_text("button_connectingDB"));
            button_connectDB.setEnabled(false);
            db_configuration();
            if(playerControl.connect_db()){
                lock_db_input();
            }else{
                GeneralDialog.get().popup("db_login_failed");
                button_connectDB.setText(PlayerDialog.get().get_text("button_connectDB"));
                button_connectDB.setEnabled(true);
            }
        }else{
            button_connectDB.setText(PlayerDialog.get().get_text("button_disconnectingDB"));
            button_connectDB.setEnabled(false);
            if(playerControl.DB_source()){
                tableModel.update_data(new TreeMap<>());
                table_data.setModel(tableModel);
            }
            if(playerControl.disconnect_db()){
                unlock_db_input();
            }else{
                GeneralDialog.get().popup("db_disconnect_failed");
                button_connectDB.setText(PlayerDialog.get().get_text("button_disconnectDB"));
                button_connectDB.setEnabled(true);
            }
        }
    }

    private void db_configuration(){
        String URL = label_URL.getText() + text_URL.getText();
        switch ((String) Objects.requireNonNull(comboBox_SQL.getSelectedItem())){
            case "MySQL":
                char[] pwd = passwordField_pwd.getPassword();
                String password = new String(pwd);
                playerControl.configure_db(
                        URL,
                        text_port.getText(),
                        text_database.getText(),
                        text_user.getText(),
                        password);
                break;
            case "SQLite":
                playerControl.configure_db(URL);
                break;
        }

    }

    private void lock_db_input(){
        //when database connected
        button_connectDB.setText("Disconnect");
        button_connectDB.setEnabled(true);
        button_importDB.setEnabled(true);
        text_URL.setEnabled(false);
        text_database.setEnabled(false);
        text_port.setEnabled(false);
        text_user.setEnabled(false);
        passwordField_pwd.setEnabled(false);
        comboBox_SQL.setEnabled(false);
    }

    private void unlock_db_input(){
        button_connectDB.setText("Connect to DB");
        button_connectDB.setEnabled(true);
        text_URL.setEnabled(true);
        text_database.setEnabled(true);
        text_user.setEnabled(true);
        text_port.setEnabled(true);
        passwordField_pwd.setEnabled(true);
        button_importDB.setEnabled(false);
        comboBox_SQL.setEnabled(true);
        configure_title();
    }

    private void configure_title(){
        TitledBorder border = BorderFactory.createTitledBorder(playerControl.data_source((String)comboBox_SQL.getSelectedItem()));
        main_panel.setBorder(border);
    }
}


