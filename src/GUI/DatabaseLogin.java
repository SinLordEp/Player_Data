package GUI;

import Interface.CallBack;
import model.DataInfo;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

/**
 * The {@code DatabaseLogin} class provides a UI dialog for logging into a database by collecting
 * connection information such as URL, port, database name, username, and password. It allows the
 * user to submit or cancel the login operation.
 * <p>
 * This dialog is designed to support different database dialects (e.g., MySQL, SQLite) by
 * dynamically configuring the input fields and labels based on the provided {@code DatabaseInfo}.
 */
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
    private JPanel panel_info;
    private JPanel panel_button;
    private final DataInfo dataInfo;

    /**
     * Creates a new {@code DatabaseLogin} dialog for user interaction to collect database login
     * information and provides callbacks for submission or cancellation.
     *
     * @param dataInfo an instance of {@code DatabaseInfo} containing initial database configuration details.
     * @param callBack a {@code CallBack<DatabaseInfo>} implementation used for handling subsequent
     *                 actions upon submission or cancellation, invoking {@code onSubmit(T object)} or {@code onCancel()}.
     */
    public DatabaseLogin(DataInfo dataInfo, CallBack<DataInfo> callBack) {
        this.dataInfo = dataInfo;
        setTitle(GeneralText.fetch().getText("db_login_title"));
        UiUtils.setLabelButtonText(GeneralText.fetch(), panel_info, panel_button);
        configureTextFieldText();
        setContentPane(panel_main);
        setModal(true);
        getRootPane().setDefaultButton(button_submit);
        button_submit.addActionListener(_ -> onOK(callBack));
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

    /**
     * Handles the submission of database login information. This method extracts data
     * from the dialog's input fields to populate the {@code DatabaseInfo} object. If any
     * required fields are blank, a popup message notifies the user. Upon successful
     * validation, it invokes the {@code onSubmit} method of the provided callback
     * with the populated {@code DatabaseInfo} object.
     *
     * @param callBack a {@code CallBack<DatabaseInfo>} implementation to handle the submitted
     *                 database login information or perform actions based on the user's input.
     */
    private void onOK(CallBack<DataInfo> callBack) {
        dispose();
        if(hasBlank()){
            GeneralText.fetch().popup("db_field_empty");
            return;
        }
        dataInfo.setUrl(text_url.getText());
        if(text_port.isVisible()){
            dataInfo.setPort(text_port.getText());
        }
        if(text_database.isVisible()){
            dataInfo.setDatabase(text_database.getText());
        }
        if(text_user.isVisible()){
            dataInfo.setUser(text_user.getText());
        }
        if(passwordField_pwd.isVisible()){
            String pwd = new String(this.passwordField_pwd.getPassword());
            dataInfo.setPassword(pwd);
        }
        if(callBack != null){
            callBack.onSubmit(dataInfo);
        }
    }

    /**
     * Handles the cancellation of the database login dialog process.
     * This method invokes the {@code onCancel} method of the provided callback
     * to notify that the database login operation has been canceled and releases resources associated with the dialog.
     *
     */
    private void onCancel() {
        dispose();
    }

    /**
     * Configures the text values for input fields in the {@code DatabaseLogin} dialog
     * based on the SQL dialect specified in {@code databaseInfo}.
     * <p>
     * This method determines the SQL dialect by invoking {@code databaseInfo.getDialect()}
     * and adjusts the visibility and content of text fields accordingly. It handles
     * separate configurations for MySQL and SQLite using {@code configureMultipleField}
     * and {@code configureSQLite} methods.
     * <p>
     * If the dialect is MySQL, it calls {@code configureMultipleField}, which sets up the URL, port,
     * database name, user credentials, and password fields, ensuring the relevant fields are visible.
     * If the dialect is SQLite, it calls {@code configureSQLite}, which configures the URL field
     * while keeping other fields hidden or unspecified.
     */
    private void configureTextFieldText(){
        text_url.setText(dataInfo.getUrl());
        if(dataInfo.getPort() != null){
            text_port.setText(dataInfo.getPort());
            text_port.setVisible(true);
            label_port.setVisible(true);
        }
        if(dataInfo.getDatabase() != null){
            text_database.setText(dataInfo.getDatabase());
            text_database.setVisible(true);
            label_database.setVisible(true);
        }
        if(dataInfo.getUser() != null){
            text_user.setText(dataInfo.getUser());
            text_user.setVisible(true);
            label_user.setVisible(true);
        }
        if(dataInfo.getPassword() != null){
            passwordField_pwd.setText(dataInfo.getPassword());
            passwordField_pwd.setVisible(true);
            label_pwd.setVisible(true);
        }

    }

    /**
     * Checks if any of the required input fields in the database login form are blank.
     * This method evaluates the text content of fields including {@code text_url},
     * {@code text_port}, {@code text_database}, {@code text_user}, and {@code passwordField_pwd}.
     * It also considers if the fields are enabled before performing the check.
     * <p>
     * For the password field, it checks whether the string representation of
     * {@code passwordField_pwd.getPassword()} is blank.
     *
     * @return {@code true} if any of the enabled fields are blank, otherwise {@code false}.
     */
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
