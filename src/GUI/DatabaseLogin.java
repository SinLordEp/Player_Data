package GUI;

import Interface.GeneralControl;
import data.DataSource;
import main.OperationException;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;

public class DatabaseLogin {
    private JFrame frame;
    private JPanel panel_main;
    private JTextField text_url;
    private JLabel label_url;
    private JTextField text_port;
    private JTextField text_database;
    private JTextField text_user;
    private JPasswordField passwordField_pwd;
    private JButton button_submit;
    private JLabel label_port;
    private JLabel label_database;
    private JLabel label_user;
    private JLabel label_pwd;
    private final GeneralControl control;
    private final HashMap<String,String> login_info;

    public DatabaseLogin(GeneralControl control, DataSource dataSource) {
        this.control = control;
        this.login_info = control.getDefaultDatabase();
        this.panel_main.setBorder(BorderFactory.createTitledBorder(dataSource.toString()));
    }

    public void run() {
        configureLabelText();
        configureTextFieldText();
        configureListener();
        frame = new JFrame(GeneralDialog.getDialog().getText("db_login_title"));
        frame.setContentPane(panel_main);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                throw new OperationException("Operation cancelled by user");
            }
        });
    }

    private void configureLabelText(){
        this.label_url.setText(GeneralDialog.getDialog().getText("label_url"));
        this.label_port.setText(GeneralDialog.getDialog().getText("label_port"));
        this.label_database.setText(GeneralDialog.getDialog().getText("label_database"));
        this.label_user.setText(GeneralDialog.getDialog().getText("label_user"));
        this.label_pwd.setText(GeneralDialog.getDialog().getText("label_pwd"));
    }

    private void configureTextFieldText(){
        this.text_url.setText(login_info.get("text_url"));
        if(login_info.containsKey("text_port")){
            this.text_port.setText(login_info.get("text_port"));
        }else{
            this.text_port.setVisible(false);
            this.label_port.setVisible(false);
        }
        if(login_info.containsKey("text_database")){
            this.text_database.setText(login_info.get("text_database"));
        }else {
            this.text_database.setVisible(false);
            this.label_database.setVisible(false);
        }
        if(login_info.containsKey("text_user")){
            this.text_user.setText(login_info.get("text_user"));
        }else {
            this.text_user.setVisible(false);
            this.label_user.setVisible(false);
        }
        if(login_info.containsKey("passwordField_pwd")){
            this.passwordField_pwd.setText(login_info.get("passwordField_pwd"));
        }else{
            this.passwordField_pwd.setVisible(false);
            this.label_pwd.setVisible(false);
        }
    }
    public boolean hasBlank(){
        boolean blank = text_url.getText().isBlank();
        if(text_port.isEnabled() && text_port.getText().isBlank()) blank = true;
        if(text_database.isEnabled() && text_database.getText().isBlank()) blank = true;
        if(text_user.isEnabled() && text_user.getText().isBlank()) blank = true;
        if(passwordField_pwd.isEnabled() && (Arrays.toString(passwordField_pwd.getPassword()).isBlank())) blank = true;
        return blank;
    }

    private void configureListener(){
        button_submit.addActionListener(_ ->{
            submit();
            control.connectDB();
        });
    }

    private void submit() {
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
            login_info.put("passwordField_pwd", pwd);
        }
        frame.dispose();
    }
}
