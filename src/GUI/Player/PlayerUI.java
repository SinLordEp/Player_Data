package GUI.Player;

import Interface.GeneralUI;
import control.PlayerControl;
import data.DataSource;
import data.database.SqlDialect;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.TreeMap;

public class PlayerUI implements GeneralUI {
    private final PlayerControl playerControl;
    private JTable table_data;

    private JButton button_add;
    private JButton button_modify;
    private JButton button_delete;
    private JButton button_export;
    private JButton button_connectDB;
    private JButton button_import;
    private JButton button_createFile;
    private JButton button_language;


    private JLabel label_search;
    private JTextField field_search;

    private JPanel main_panel;
    private JScrollPane scroll_data;
    private JComboBox<SqlDialect> comboBox_SQL;
    private JComboBox<DataSource> comboBox_dataSource;
    private PlayerTableModel tableModel;
    private int selected_player_id;

    public PlayerUI(PlayerControl control) {
        playerControl = control;
    }

    private void initialize(){
        main_panel.setBorder(BorderFactory.createTitledBorder(PlayerDialog.getDialog().getText("default_data_source")));
        tableModel = new PlayerTableModel(new TreeMap<>());
        table_data.setModel(tableModel);
        table_data.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroll_data.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setUIText(false);
        searchListener();
        buttonListener();
        tableListener();
        comboBoxListener();
        initializeComboBox();
    }

    private void initializeComboBox(){
        for(DataSource dataSource : DataSource.values()){
            comboBox_dataSource.addItem(dataSource);
        }
        for(SqlDialect dialect : SqlDialect.values()){
            comboBox_SQL.addItem(dialect);
        }
        comboBox_dataSource.setSelectedItem(DataSource.NONE);
        disableSQL();
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
        configureTitle();
    }

    public void setUIText(boolean isDBConnected){
        button_add.setText(PlayerDialog.getDialog().getText("button_add"));
        button_modify.setText(PlayerDialog.getDialog().getText("button_modify"));
        button_delete.setText(PlayerDialog.getDialog().getText("button_delete"));
        button_export.setText(PlayerDialog.getDialog().getText("button_export"));
        if(!isDBConnected){
            button_connectDB.setText(PlayerDialog.getDialog().getText("button_connectDB"));
        }else{
            button_connectDB.setText(PlayerDialog.getDialog().getText("button_disconnectDB"));
        }
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

        button_connectDB.addActionListener(_ -> playerControl.DBConnection());

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

    private void comboBoxListener(){
        comboBox_dataSource.addActionListener(_ -> {
            switch ((DataSource) Objects.requireNonNull(comboBox_dataSource.getSelectedItem())){
                case NONE:
                    disableSQL();
                    button_import.setEnabled(false);
                    break;
                case FILE:
                    disableSQL();
                    button_import.setEnabled(true);
                    break;
                case DATABASE, HIBERNATE:
                    comboBox_SQL.setEnabled(true);
                    break;
            }
            playerControl.setDataSource((DataSource) comboBox_dataSource.getSelectedItem());
        });
        comboBox_SQL.addActionListener(_ -> {
            if(comboBox_SQL.isEnabled()){
                switch ((SqlDialect) Objects.requireNonNull(comboBox_SQL.getSelectedItem())){
                    case NONE:
                        button_import.setEnabled(false);
                        break;
                    case MYSQL, SQLITE:
                        button_import.setEnabled(true);
                        playerControl.setSQLDialect((SqlDialect)comboBox_SQL.getSelectedItem());
                }
            }
        });
    }

    private void disableSQL(){
        comboBox_SQL.setEnabled(false);
        comboBox_SQL.setSelectedItem(SqlDialect.NONE);
    }

   /* public void configureMySQL(){
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
    }
*/

    public void connecting(){
        button_connectDB.setText(PlayerDialog.getDialog().getText("button_connectingDB"));
        button_connectDB.setEnabled(false);
    }

    public void disconnecting(){
        button_connectDB.setText(PlayerDialog.getDialog().getText("button_disconnectingDB"));
        button_connectDB.setEnabled(false);
    }

    public void connected(){
        button_connectDB.setText(PlayerDialog.getDialog().getText("button_disconnectDB"));
        inputSwitch(false);
    }

    public void disconnected(){
        tableModel.update_data(new TreeMap<>());
        table_data.setModel(tableModel);
        button_connectDB.setText(PlayerDialog.getDialog().getText("button_connectDB"));
        configureTitle();
        inputSwitch(true);
    }

    private void inputSwitch(boolean state){
        button_connectDB.setEnabled(true);
        button_import.setEnabled(!state);
        comboBox_SQL.setEnabled(state);
    }

    /*public void setDBLoginInfo(){
        switch ((String) Objects.requireNonNull(comboBox_SQL.getSelectedItem())){
            case "MySQL":
                playerControl.configureDB(
                        label_URL.getText() + text_URL.getText(),
                        text_port.getText(),
                        text_database.getText(),
                        text_user.getText(),
                        passwordField_pwd.getPassword());
                break;
            case "SQLite":
                playerControl.configureDB(label_URL.getText() + text_URL.getText());
                break;
        }

    }*/

    private void configureTitle(){
        main_panel.setBorder(BorderFactory.createTitledBorder(PlayerDialog.getDialog().getText("data_source") + playerControl.getDataSource()));
    }

    public void changeLanguage(boolean isDBConnected){
        setUIText(isDBConnected);
        tableModel.language_changed();
    }
}


