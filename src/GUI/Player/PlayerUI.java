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
    private JButton button_language;

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
        frame = new JFrame(PlayerDialog.get().getText("frame_title"));
        TitledBorder border = BorderFactory.createTitledBorder(PlayerDialog.get().getText("default_data_source"));
        main_panel.setBorder(border);
        tableModel = new PlayerTableModel(new TreeMap<>());
        table_data.setModel(tableModel);
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setUIText();
        dbInitialize();
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
        playerControl.refreshDA();
        tableModel.update_data(playerControl.getMap());
        table_data.setModel(tableModel);
        configureTitle();
    }

    public void setUIText(){
        button_add.setText(PlayerDialog.get().getText("button_add"));
        button_modify.setText(PlayerDialog.get().getText("button_modify"));
        button_delete.setText(PlayerDialog.get().getText("button_delete"));
        button_export.setText(PlayerDialog.get().getText("button_export"));
        if(!db_connected){
            button_connectDB.setText(PlayerDialog.get().getText("button_connectDB"));
        }else{
            button_connectDB.setText(PlayerDialog.get().getText("button_disconnectDB"));
        }
        button_importFile.setText(PlayerDialog.get().getText("button_importFile"));
        button_importDB.setText(PlayerDialog.get().getText("button_importDB"));
        button_createFile.setText(PlayerDialog.get().getText("button_createFile"));
        button_language.setText(PlayerDialog.get().getText("button_language"));
        label_port.setText(PlayerDialog.get().getText("label_port"));
        label_search.setText(PlayerDialog.get().getText("label_search"));
        label_pwd.setText(PlayerDialog.get().getText("label_pwd"));
        label_user.setText(PlayerDialog.get().getText("label_user"));
        label_database.setText(PlayerDialog.get().getText("label_database"));
        frame.pack();
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
            try {
                playerControl.createPlayer();
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_modify.addActionListener(_ -> {
            try {
                playerControl.modifyPlayer(selected_player_id);
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_delete.addActionListener(_ -> {
            try {
                playerControl.delete(selected_player_id);
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_export.addActionListener(_ -> {
            try {
                playerControl.export();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_connectDB.addActionListener(_ -> {
            try {
                dbConnect();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_createFile.addActionListener(_ -> {
            try {
                playerControl.createFile();
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
        });

        button_importFile.addActionListener(_ -> {
            try {
                playerControl.importFile();
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
            button_importDB.setEnabled(true);
        });

        button_importDB.addActionListener(_ -> {
            playerControl.importDB();
            try {
                refresh();
            } catch (Exception e) {
                GeneralDialog.get().message(e.getMessage());
            }
            button_importDB.setEnabled(false);
        });

        button_language.addActionListener(_ -> {
            playerControl.changeLanguage();
            setUIText();
            tableModel.language_changed();
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

    private void comboBoxListener(){
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

    private void dbInitialize(){
        comboBox_SQL.addItem("MySQL");
        comboBox_SQL.addItem("SQLite");
        searchListener();
        buttonListener();
        tableListener();
        comboBoxListener();
        comboBox_SQL.setSelectedIndex(0);
        db_connected = false;
    }

    private boolean dbIsBlank(){
        return (text_URL.getText().isBlank()) && (text_database.getText().isBlank()) && (text_port.getText().isBlank()) && (text_user.getText().isBlank()) && (Arrays.toString(passwordField_pwd.getPassword()).isBlank());
    }

    private void dbConnect() {
        if (!db_connected) {
            if(dbIsBlank()){
                GeneralDialog.get().popup("db_field_empty");
                return;
            }
            button_connectDB.setText(PlayerDialog.get().getText("button_connectingDB"));
            button_connectDB.setEnabled(false);
            dbConfiguration();
            if(playerControl.connectDB()){
                lockInput();
                db_connected = true;
            }else{
                GeneralDialog.get().popup("db_login_failed");
                button_connectDB.setText(PlayerDialog.get().getText("button_connectDB"));
                button_connectDB.setEnabled(true);
            }
        }else{
            button_connectDB.setText(PlayerDialog.get().getText("button_disconnectingDB"));
            button_connectDB.setEnabled(false);
            if(playerControl.DBSource()){
                tableModel.update_data(new TreeMap<>());
                table_data.setModel(tableModel);
            }
            if(playerControl.disconnectDB()){
                unlockInput();
                db_connected = false;
            }else{
                GeneralDialog.get().popup("db_disconnect_failed");
                button_connectDB.setText(PlayerDialog.get().getText("button_disconnectDB"));
                button_connectDB.setEnabled(true);
            }
        }
    }

    private void dbConfiguration(){
        String URL = label_URL.getText() + text_URL.getText();
        switch ((String) Objects.requireNonNull(comboBox_SQL.getSelectedItem())){
            case "MySQL":
                char[] pwd = passwordField_pwd.getPassword();
                String password = new String(pwd);
                playerControl.configureDB(
                        URL,
                        text_port.getText(),
                        text_database.getText(),
                        text_user.getText(),
                        password);
                break;
            case "SQLite":
                playerControl.configureDB(URL);
                break;
        }

    }

    private void lockInput(){
        //when database connected
        button_connectDB.setText(PlayerDialog.get().getText("button_disconnectDB"));
        button_connectDB.setEnabled(true);
        button_importDB.setEnabled(true);
        text_URL.setEnabled(false);
        text_database.setEnabled(false);
        text_port.setEnabled(false);
        text_user.setEnabled(false);
        passwordField_pwd.setEnabled(false);
        comboBox_SQL.setEnabled(false);
    }

    private void unlockInput(){
        button_connectDB.setText(PlayerDialog.get().getText("button_connectDB"));
        button_connectDB.setEnabled(true);
        text_URL.setEnabled(true);
        text_database.setEnabled(true);
        text_user.setEnabled(true);
        text_port.setEnabled(true);
        passwordField_pwd.setEnabled(true);
        button_importDB.setEnabled(false);
        comboBox_SQL.setEnabled(true);
        configureTitle();
    }

    private void configureTitle(){
        TitledBorder border = BorderFactory.createTitledBorder(playerControl.dataSource((String)comboBox_SQL.getSelectedItem()));
        main_panel.setBorder(border);
    }
}


