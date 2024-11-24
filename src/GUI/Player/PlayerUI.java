package GUI.Player;

import Interface.GeneralUI;
import control.PlayerControl;
import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;

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
    private JButton button_import;
    private JButton button_createFile;
    private JButton button_language;


    private JLabel label_search;
    private JTextField field_search;

    private JPanel main_panel;
    private JScrollPane scroll_data;
    private JComboBox<Object> comboBox_dataType;
    private JComboBox<DataSource> comboBox_dataSource;
    private JLabel label_dataSource;
    private JLabel label_dataType;
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
        comboBoxListener();
        initializeDataSourceComboBox();
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

    private void initializeDataSourceComboBox(){
        for(DataSource dataSource : DataSource.values()){
            comboBox_dataSource.addItem(dataSource);
        }
        comboBox_dataSource.setSelectedItem(DataSource.NONE);
    }

    private void configureDataType(DataSource dataSource) {
        button_createFile.setEnabled(false);
        button_import.setEnabled(false);
        switch(dataSource){
            case NONE:
                comboBox_dataType.setEnabled(false);
                label_dataType.setText(PlayerDialog.getDialog().getText("label_dataType"));
                return;
            case FILE:
                for(FileType fileType : FileType.values()){
                    comboBox_dataType.addItem(fileType);
                }
                label_dataType.setText(PlayerDialog.getDialog().getText("label_file_type"));
                break;
            case DATABASE, HIBERNATE:
                for(SqlDialect sqlDialect : SqlDialect.values()){
                    comboBox_dataType.addItem(sqlDialect);
                }
                label_dataType.setText(PlayerDialog.getDialog().getText("label_sql_dialect"));
                break;
        }
        comboBox_dataType.setEnabled(true);
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
        label_dataSource.setText(PlayerDialog.getDialog().getText("label_dataSource"));
        switch (comboBox_dataSource.getSelectedItem()){
            case DataSource.FILE -> label_dataType.setText(PlayerDialog.getDialog().getText("label_file_type"));
            case DataSource.DATABASE, DataSource.HIBERNATE -> label_dataType.setText(PlayerDialog.getDialog().getText("label_sql_dialect"));
            case null, default -> label_dataType.setText(PlayerDialog.getDialog().getText("label_dataType"));
        }
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

    private void comboBoxListener(){
        comboBox_dataSource.addItemListener(_ -> {
            comboBox_dataType.removeAllItems();
            configureDataType((DataSource) Objects.requireNonNull(comboBox_dataSource.getSelectedItem()));
        });
        comboBox_dataType.addItemListener(_ -> {
            switch(comboBox_dataType.getSelectedItem()){
                case SqlDialect.NONE, FileType.NONE:
                case null:
                    return;
                case FileType ignore:
                    button_createFile.setEnabled(true);
                    button_import.setEnabled(true);
                    break;
                case SqlDialect ignore:
                    button_import.setEnabled(true);
                    break;
                default:
            }
        });
    }

    public DataSource getDataSource(){
        return (DataSource) comboBox_dataSource.getSelectedItem();
    }

    public SqlDialect getSQLDialect(){
        return (SqlDialect) comboBox_dataType.getSelectedItem();
    }

    public FileType getFileType(){
        return (FileType) comboBox_dataType.getSelectedItem();
    }

    public void changeLanguage(){
        setUIText();
        tableModel.language_changed();
    }
}


