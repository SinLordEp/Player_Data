package GUI;

import Interface.CallBack;
import model.DatabaseInfo;

import javax.swing.*;
import java.awt.event.*;
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
    private final DatabaseInfo databaseInfo;

    /**
     * Creates a new {@code DatabaseLogin} dialog for user interaction to collect database login
     * information and provides callbacks for submission or cancellation.
     *
     * @param databaseInfo an instance of {@code DatabaseInfo} containing initial database configuration details.
     * @param callBack a {@code CallBack<DatabaseInfo>} implementation used for handling subsequent
     *                 actions upon submission or cancellation, invoking {@code onSubmit(T object)} or {@code onCancel()}.
     */
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

    /**
     * Handles the cancellation of the database login dialog process.
     * This method invokes the {@code onCancel} method of the provided callback
     * to notify that the database login operation has been canceled and releases resources associated with the dialog.
     *
     * @param callBack a {@code CallBack<DatabaseInfo>} implementation to handle the cancellation
     *                 and perform any necessary actions in response to the operation being terminated.
     */
    private void onCancel(CallBack<DatabaseInfo> callBack) {
        callBack.onCancel();
        dispose();
    }

    /**
     * Configures the text for various labels in the {@code DatabaseLogin} dialog.
     * This method sets the localized text for label components such as {@code label_url},
     * {@code label_port}, {@code label_database}, {@code label_user}, and {@code label_pwd}.
     * It retrieves the localized text values using the {@code GeneralText.getDialog()} method
     * and assigns them to the corresponding labels to ensure proper user interface localization.
     */
    private void configureLabelText(){
        this.label_url.setText(GeneralText.getDialog().getText("label_url"));
        this.label_port.setText(GeneralText.getDialog().getText("label_port"));
        this.label_database.setText(GeneralText.getDialog().getText("label_database"));
        this.label_user.setText(GeneralText.getDialog().getText("label_user"));
        this.label_pwd.setText(GeneralText.getDialog().getText("label_pwd"));
    }

    /**
     * Configures the text values for input fields in the {@code DatabaseLogin} dialog
     * based on the SQL dialect specified in {@code databaseInfo}.
     * <p>
     * This method determines the SQL dialect by invoking {@code databaseInfo.getDialect()}
     * and adjusts the visibility and content of text fields accordingly. It handles
     * separate configurations for MySQL and SQLite using {@code configureMySQL}
     * and {@code configureSQLite} methods.
     * <p>
     * If the dialect is MySQL, it calls {@code configureMySQL}, which sets up the URL, port,
     * database name, user credentials, and password fields, ensuring the relevant fields are visible.
     * If the dialect is SQLite, it calls {@code configureSQLite}, which configures the URL field
     * while keeping other fields hidden or unspecified.
     */
    private void configureTextFieldText(){
        switch (databaseInfo.getDialect()){
            case MYSQL -> configureMySQL();
            case SQLITE -> configureSQLite();
        }

    }

    /**
     * Configures the input fields and visibility settings in the {@code DatabaseLogin} dialog
     * specifically for MySQL connections.
     * <p>
     * This method updates the text and visibility of components based on
     * the database configuration provided by {@code databaseInfo}.
     * It sets the URL, port, database name, user credentials, and password fields
     * to the corresponding values retrieved from {@code databaseInfo}.
     * Additionally, it ensures that all relevant fields and labels are made visible
     * for user interaction.
     * <p>
     * The method is invoked internally, typically from the {@code configureTextFieldText()} method,
     * when the SQL dialect in use is determined to be MySQL.
     */
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

    /**
     * Configures the SQLite-specific settings in the {@code DatabaseLogin} dialog by
     * updating the URL text field with the SQLite database URL.
     * <p>
     * This method retrieves the database URL by calling {@code databaseInfo.getUrl()}
     * and sets the value in the {@code text_url} field. It ensures the URL is properly
     * displayed to the user for SQLite connections.
     * <p>
     * This method is typically invoked internally, such as within the
     * {@code configureTextFieldText()} method, when the SQL dialect is determined
     * to be SQLite.
     */
    private void configureSQLite(){
        text_url.setText(databaseInfo.getUrl());
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
