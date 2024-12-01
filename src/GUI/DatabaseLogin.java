package GUI;

import Interface.CallBack;
import model.DatabaseInfo;

import javax.swing.*;
import java.awt.event.*;
import java.util.Arrays;

public class DatabaseLogin extends JDialog {
    private JPanel panel_main;
    private JButton button_submit;
    private JButton button_cancel;
    private JLabel label_url;
    private JTextField text_url;
    private JLabel label_port;
    private JTextField text_port;
    private JLabel label_database;
    private JTextField text_database;
    private JLabel label_user;
    private JTextField text_user;
    private JLabel label_pwd;
    private JPasswordField passwordField_pwd;
    private final DatabaseInfo databaseInfo;

    public DatabaseLogin(DatabaseInfo databaseInfo, CallBack<DatabaseInfo> callBack) {
        this.databaseInfo = databaseInfo;
        setTitle(GeneralText.getDialog().getText("db_login_title"));
        configureLabelText();
        configureTextFieldText();
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        button_submit.addActionListener(_ -> onOK(callBack));
        button_cancel.addActionListener(_ -> onCancel(callBack));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel(callBack);
            }
        });
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void onOK(CallBack<DatabaseInfo> callBack) {
        if(hasBlank()){
            GeneralText.getDialog().popup("db_field_empty");
            return;
        }
        databaseInfo.setUrl(text_url.getText());
        if(text_port.isVisible()){
            databaseInfo.setPort(text_port.getText());
        }
        if(text_database.isVisible()){
            databaseInfo.setDatabase(text_database.getText());
        }
        if(text_user.isVisible()){
            databaseInfo.setUser(text_user.getText());
        }
        if(passwordField_pwd.isVisible()){
            String pwd = new String(this.passwordField_pwd.getPassword());
            databaseInfo.setPassword(pwd);
        }
        if(callBack != null){
            callBack.onSubmit(databaseInfo);
        }
        dispose();
    }

    private void onCancel(CallBack<DatabaseInfo> callBack) {
        callBack.onCancel();
        dispose();
    }

    private void configureLabelText(){
        this.label_url.setText(GeneralText.getDialog().getText("label_url"));
        this.label_port.setText(GeneralText.getDialog().getText("label_port"));
        this.label_database.setText(GeneralText.getDialog().getText("label_database"));
        this.label_user.setText(GeneralText.getDialog().getText("label_user"));
        this.label_pwd.setText(GeneralText.getDialog().getText("label_pwd"));
    }

    private void configureTextFieldText(){
        switch (databaseInfo.getDialect()){
            case MYSQL -> configureMySQL();
            case SQLITE -> configureSQLite();
        }

    }

    private void configureMySQL(){
        text_url.setText(databaseInfo.getUrl());
        text_port.setText(databaseInfo.getPort());
        text_port.setVisible(true);
        label_port.setVisible(true);
        text_database.setText(databaseInfo.getDatabase());
        text_database.setVisible(true);
        label_database.setVisible(true);
        text_user.setText(databaseInfo.getUser());
        text_user.setVisible(true);
        label_user.setVisible(true);
        passwordField_pwd.setText(databaseInfo.getPassword());
        passwordField_pwd.setVisible(true);
        label_pwd.setVisible(true);
    }

    private void configureSQLite(){
        text_url.setText(databaseInfo.getUrl());
    }

    public boolean hasBlank(){
        boolean blank = text_url.getText().isBlank();
        if(text_port.isEnabled() && text_port.getText().isBlank()) {
            blank = true;
        }
        if(text_database.isEnabled() && text_database.getText().isBlank()) {
            blank = true;
        }
        if(text_user.isEnabled() && text_user.getText().isBlank()) {
            blank = true;
        }
        if(passwordField_pwd.isEnabled() && (Arrays.toString(passwordField_pwd.getPassword()).isBlank())) {
            blank = true;
        }
        return blank;
    }

}
