package GUI;

import exceptions.OperationCancelledException;

import javax.swing.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashMap;

public class DataBaseLogin extends JDialog {
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
    private final HashMap<String,String> login_info;
    private boolean valid = false;

    public DataBaseLogin(HashMap<String,String> login_info) {
        this.login_info = login_info;
        setTitle(GeneralDialog.getDialog().getText("db_login_title"));
        configureLabelText();
        configureTextFieldText();
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        button_submit.addActionListener(_ -> onOK());
        button_cancel.addActionListener(_ -> onCancel());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    private void onOK() {
        if(hasBlank()){
            GeneralDialog.getDialog().popup("db_field_empty");
            return;
        }
        login_info.put("text_url", this.text_url.getText());
        if(text_port.isVisible()){
            login_info.put("text_port", this.text_port.getText());
        }
        if(text_database.isVisible()){
            login_info.put("text_database", this.text_database.getText());
        }
        if(text_user.isVisible()){
            login_info.put("text_user", this.text_user.getText());
        }
        if(passwordField_pwd.isVisible()){
            String pwd = new String(this.passwordField_pwd.getPassword());
            login_info.put("text_pwd", pwd);
        }
        valid = true;
        dispose();
    }

    private void onCancel() {
        dispose();
        throw new OperationCancelledException();
    }

    private void configureLabelText(){
        this.label_url.setText(GeneralDialog.getDialog().getText("label_url"));
        this.label_port.setText(GeneralDialog.getDialog().getText("label_port"));
        this.label_database.setText(GeneralDialog.getDialog().getText("label_database"));
        this.label_user.setText(GeneralDialog.getDialog().getText("label_user"));
        this.label_pwd.setText(GeneralDialog.getDialog().getText("label_pwd"));
    }

    private void configureTextFieldText(){
        text_url.setText(login_info.get("text_url"));
        if(login_info.size()== 1){
            return;
        }
        if(login_info.containsKey("text_port")){
            text_port.setText(login_info.get("text_port"));
            text_port.setVisible(true);
            label_port.setVisible(true);
        }
        if(login_info.containsKey("text_database")){
            text_database.setText(login_info.get("text_database"));
            text_database.setVisible(true);
            label_database.setVisible(true);
        }
        if(login_info.containsKey("text_user")){
            text_user.setText(login_info.get("text_user"));
            text_user.setVisible(true);
            label_user.setVisible(true);
        }
        if(login_info.containsKey("text_pwd")){
            passwordField_pwd.setText(login_info.get("text_pwd"));
            passwordField_pwd.setVisible(true);
            label_pwd.setVisible(true);
        }
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
