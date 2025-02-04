package GUI;

import GUI.Player.PlayerText;
import Interface.DataSourceCallBack;
import data.DataSource;
import data.database.SqlDialect;
import data.file.FileType;
import data.http.PhpType;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

/**
 * The {@code DataSourceChooser} class is a dialog designed to allow users to
 * choose a data source and its corresponding data type. It provides a graphical
 * user interface (GUI) for selection, with callback integration for handling
 * user input.
 */
public class DataSourceChooser extends JDialog {
    private JPanel panel_main;
    private JButton button_submit;
    private JButton button_cancel;
    private JLabel label_dataSource;
    private JComboBox<DataSource> comboBox_dataSource;
    private JLabel label_dataType;
    private JComboBox<Object> comboBox_dataType;
    private JPanel panel_info;
    private JPanel panel_button;
    private DataSource dataSource;
    private Object dataType;

    /**
     * Constructs a {@code DataSourceChooser} dialog that allows the user to choose a data source
     * and optionally set the data type. Based on the provided {@code DataSource}, the dialog's
     * components are initialized and configured, with a callback mechanism for submission
     * or cancellation of the dialog's operations.
     *
     * @param dataSource The initial {@code DataSource} to be pre-selected in the combo box. If
     *                   the value is {@code DataSource.FILE}, the data source selection is
     *                   locked and cannot be changed.
     * @param callback   The callback instance implementing {@code DataSourceCallBack<DataSource, Object>}
     *                   to handle actions for submission and cancellation. Interaction, such as
     *                   submitting the selected data source and data type, invokes the {@code onSubmit}
     *                   method of the callback. Cancellation events invoke the {@code onCancel} method.
     */
    public DataSourceChooser(DataSource dataSource, DataSourceCallBack<DataSource, Object> callback) {
        initialize(callback);
        this.dataSource = dataSource;
        if(dataSource == DataSource.FILE){
            comboBox_dataSource.setSelectedItem(DataSource.FILE);
            comboBox_dataSource.setEnabled(false);
        }
        setVisible(true);
    }

    /**
     * Initializes and configures the dialog components, including setting up UI text,
     * populating combo boxes, defining event listeners, and preparing the dialog for use.
     * This method ensures proper component initialization and registers the necessary
     * callback handlers for submission and cancellation actions.
     *
     * @param callback The callback instance of {@code DataSourceCallBack<DataSource, Object>}
     *                 that handles actions when the dialog is submitted via {@code onOK(callback)}
     *                 or canceled via {@code onCancel(callback)}. Submission triggers
     *                 {@code callback.onSubmit(dataSource, dataType)}, while cancellation
     *                 calls {@code callback.onCancel()}.
     */
    private void initialize(DataSourceCallBack<DataSource, Object> callback){
        UiUtils.setLabelButtonText(PlayerText.getDialog(), panel_info, panel_button);
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

    /**
     * Handles the submission action when the dialog's "OK" button is triggered.
     * This method invokes the {@code onSubmit} method of the provided callback
     * with the currently selected {@code dataSource} and {@code dataType}.
     * After submission, it disposes of the dialog.
     *
     * @param callback The callback instance of {@code DataSourceCallBack<DataSource, Object>}.
     *                 If not null, this method calls {@code callback.onSubmit(dataSource, dataType)}
     *                 to pass the selected data source and data type.
     */
    private void onOK(DataSourceCallBack<DataSource, Object> callback) {
        if(callback != null){
            callback.onSubmit(dataSource, dataType);
        }
        dispose();
    }

    /**
     * Handles the cancellation action for the dialog. This method resets the {@code dataSource}
     * field to {@code DataSource.NONE}, invokes the {@code onCancel()} method of the provided
     * callback to notify the caller about the cancellation event, and releases any associated
     * resources by calling {@code dispose()}.
     *
     * @param callback The instance of {@code DataSourceCallBack<DataSource, Object>} that receives
     *                 the cancellation event through its {@code onCancel()} method.
     */
    private void onCancel(DataSourceCallBack<DataSource, Object> callback) {
        dataSource = DataSource.NONE;
        callback.onCancel();
        dispose();
    }

    /**
     * Populates and initializes the {@code comboBox_dataSource} component with all available
     * {@code DataSource} values. Iterates through the {@code DataSource} enumeration and adds
     * each value to the combo box as an item.
     * <p>
     * After populating the combo box, the {@code DataSource.NONE} value is set as the default
     * selected item to ensure no specific data source is pre-selected when this method is called.
     */
    private void initializeDataSourceComboBox(){
        for(DataSource dataSource : DataSource.values()){
            comboBox_dataSource.addItem(dataSource);
        }
        comboBox_dataSource.setSelectedItem(DataSource.NONE);
    }

    /**
     * Registers item listeners for {@code comboBox_dataSource} and {@code comboBox_dataType}, enabling
     * dynamic updates of the associated components and internal states based on user selection. This
     * method effectively acts as a callback mechanism for handling data source and data type changes.
     * <p>
     * The {@code comboBox_dataSource} listener invokes {@code configureDataType(DataSource)} when an item
     * is selected. This updates the data type combo box and other components based on the selected data source.
     * <p>
     * The {@code comboBox_dataType} listener invokes {@code setDataType(Object)} with the currently selected
     * item. This updates the internal {@code dataType} field and enables the submit button if the selection
     * is valid.
     * <p>
     * This method ensures that the UI and internal state are synchronized properly as the user interacts
     * with the combo boxes.
     */
    private void comboBoxListener(){
        comboBox_dataSource.addItemListener(_ -> configureDataType((DataSource) Objects.requireNonNull(comboBox_dataSource.getSelectedItem())));

        comboBox_dataType.addItemListener(_ -> setDataType(comboBox_dataType.getSelectedItem()));
    }

    /**
     * Configures the data type options and updates UI elements based on the selected {@code DataSource}.
     * This method dynamically adjusts the content of the {@code comboBox_dataType}, its enabled state,
     * and the {@code label_dataType}'s text based on the {@code DataSource} selection.
     * <p>
     * When the selected {@code DataSource} changes, the following behavior is applied:
     * - For {@code DataSource.NONE}, disables the {@code comboBox_dataType} and resets the label's text.
     * - For {@code DataSource.FILE}, populates the combo box with {@code FileType} values and updates the label.
     * - For {@code DataSource.DATABASE} and {@code DataSource.HIBERNATE}, populates the combo box with {@code SqlDialect} values and updates the label.
     * - For {@code DataSource.PHP}, populates the combo box with {@code DataType} values and updates the label.
     * <p>
     * The submit button is disabled during reconfiguration to ensure proper user interaction.
     *
     * @param dataSource The selected {@code DataSource} used to determine the available data type options
     *                   and configure related UI elements.
     */
    private void configureDataType(DataSource dataSource) {
        this.dataSource = dataSource;
        comboBox_dataType.removeAllItems();
        button_submit.setEnabled(false);
        switch(dataSource){
            case NONE:
                comboBox_dataType.setEnabled(false);
                label_dataType.setText(PlayerText.getDialog().getText("label_dataType"));
                return;
            case FILE:
                for(FileType fileType : FileType.values()){
                    comboBox_dataType.addItem(fileType);
                }
                label_dataType.setText(PlayerText.getDialog().getText("label_file_type"));
                break;
            case DATABASE, HIBERNATE:
                for(SqlDialect sqlDialect : SqlDialect.values()){
                    comboBox_dataType.addItem(sqlDialect);
                }
                label_dataType.setText(PlayerText.getDialog().getText("label_sql_dialect"));
                break;
            case PHP:
                for(PhpType dataType : PhpType.values()){
                    comboBox_dataType.addItem(dataType);
                }
                label_dataType.setText(PlayerText.getDialog().getText("label_dataType"));
                break;
            case OBJECTDB, BASEX, MONGO:
                label_dataType.setText(PlayerText.getDialog().getText("label_dataType"));
                comboBox_dataType.setEnabled(false);
                button_submit.setEnabled(true);
                return;
        }
        comboBox_dataType.setEnabled(true);
    }

    /**
     * Sets the current data type and adjusts the state of the submit button based on the provided {@code dataType}.
     * <p>
     * This method updates the internal {@code dataType} field and controls the enabled state of the "Submit" button.
     * If the provided {@code dataType} is {@code null}, {@code FileType.NONE}, or {@code SqlDialect.NONE}, the "Submit"
     * button is disabled to prevent invalid submissions. For valid {@code FileType}, {@code SqlDialect}, or {@code DataType}
     * values, the method assigns the value to the internal {@code dataType} and enables the "Submit" button.
     *
     * @param dataType The data type object representing the selected data type. Possible values include instances of
     *                 {@code FileType}, {@code SqlDialect}, or {@code DataType}. For invalid or null cases, the button
     *                 is kept disabled.
     */
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
            case PhpType ignore:
                this.dataType = dataType;
                button_submit.setEnabled(true);
                break;
            default:
        }
    }
}
