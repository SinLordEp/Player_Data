package GUI;

import GUI.Player.PlayerDialog;
import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;
import exceptions.OperationCancelledException;

import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;

public class DataSourceChooser extends JDialog {
    private JPanel panel_main;
    private JButton button_submit;
    private JButton button_cancel;
    private JLabel label_dataSource;
    private JComboBox<DataSource> comboBox_dataSource;
    private JLabel label_dataType;
    private JComboBox<Object> comboBox_dataType;
    private DataSource dataSource;
    private Object dataType;

    public DataSourceChooser(DataSource dataSource, Object dataType) {
        this.dataSource = dataSource;
        this.dataType = dataType;
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        comboBoxListener();
        button_submit.addActionListener(e -> onOK());
        button_cancel.addActionListener(e -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    //todo:
    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        dataSource = DataSource.NONE;
        dataType = switch (dataType){
            case FileType ignore -> FileType.NONE;
            case SqlDialect ignore -> SqlDialect.NONE;
            default -> null;
        };
        dispose();
    }

    private void comboBoxListener(){
        comboBox_dataSource.addItemListener(_ -> {
            configureDataType((DataSource) Objects.requireNonNull(comboBox_dataSource.getSelectedItem()));
        });

        comboBox_dataType.addItemListener(_ -> setDataType(comboBox_dataType.getSelectedItem()));
    }

    //TODO:
    private void configureDataType(DataSource dataSource) {
        this.dataSource = dataSource;
        comboBox_dataType.removeAllItems();
        button_submit.setEnabled(false);
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

    private void setDataType(Object dataType) {
        switch(dataType){
            case SqlDialect.NONE, FileType.NONE:
            case null:
                return;
            case FileType ignore:
                button_submit.setEnabled(true);
                break;
            case SqlDialect ignore:
                button_submit.setEnabled(true);
                break;
            default:
        }
    }

}
