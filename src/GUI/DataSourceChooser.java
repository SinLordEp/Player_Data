package GUI;

import Interface.DataSourceCallBack;
import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;


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

    public DataSourceChooser(DataSource dataSource, DataSourceCallBack<DataSource, Object> callback) {
        initialize(callback);
        this.dataSource = dataSource;
        if(dataSource == DataSource.FILE){
            comboBox_dataSource.setSelectedItem(DataSource.FILE);
            comboBox_dataSource.setEnabled(false);
        }
        setVisible(true);
    }

    private void initialize(DataSourceCallBack<DataSource, Object> callback){
        setUIText();
        initializeDataSourceComboBox();
        comboBoxListener();
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        button_submit.addActionListener(_ -> onOK(callback));
        button_cancel.addActionListener(_ -> onCancel(callback));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel(callback);
            }
        });
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private void onOK(DataSourceCallBack<DataSource, Object> callback) {
        if(callback != null){
            callback.onSubmit(dataSource, dataType);
        }
        dispose();
    }

    private void onCancel(DataSourceCallBack<DataSource, Object> callback) {
        dataSource = DataSource.NONE;
        callback.onCancel();
        dispose();
    }

    private void setUIText(){
        label_dataSource.setText(GeneralText.getDialog().getText("label_dataSource"));
        switch (comboBox_dataSource.getSelectedItem()){
            case DataSource.FILE -> label_dataType.setText(GeneralText.getDialog().getText("label_file_type"));
            case DataSource.DATABASE, DataSource.HIBERNATE -> label_dataType.setText(GeneralText.getDialog().getText("label_sql_dialect"));
            case null, default -> label_dataType.setText(GeneralText.getDialog().getText("label_dataType"));
        }
    }

    private void initializeDataSourceComboBox(){
        for(DataSource dataSource : DataSource.values()){
            comboBox_dataSource.addItem(dataSource);
        }
        comboBox_dataSource.setSelectedItem(DataSource.NONE);
    }

    private void comboBoxListener(){
        comboBox_dataSource.addItemListener(_ -> configureDataType((DataSource) Objects.requireNonNull(comboBox_dataSource.getSelectedItem())));

        comboBox_dataType.addItemListener(_ -> setDataType(comboBox_dataType.getSelectedItem()));
    }

    private void configureDataType(DataSource dataSource) {
        this.dataSource = dataSource;
        comboBox_dataType.removeAllItems();
        button_submit.setEnabled(false);
        switch(dataSource){
            case NONE:
                comboBox_dataType.setEnabled(false);
                label_dataType.setText(GeneralText.getDialog().getText("label_dataType"));
                return;
            case FILE:
                for(FileType fileType : FileType.values()){
                    comboBox_dataType.addItem(fileType);
                }
                label_dataType.setText(GeneralText.getDialog().getText("label_file_type"));
                break;
            case DATABASE, HIBERNATE:
                for(SqlDialect sqlDialect : SqlDialect.values()){
                    comboBox_dataType.addItem(sqlDialect);
                }
                label_dataType.setText(GeneralText.getDialog().getText("label_sql_dialect"));
                break;
        }
        comboBox_dataType.setEnabled(true);
    }

    private void setDataType(Object dataType) {
        switch(dataType){
            case SqlDialect.NONE, FileType.NONE:
            case null:
                button_submit.setEnabled(false);
                return;
            case FileType ignore:
                this.dataType = dataType;
                button_submit.setEnabled(true);
                break;
            case SqlDialect ignore:
                this.dataType = dataType;
                button_submit.setEnabled(true);
                break;
            default:
        }
    }
}
