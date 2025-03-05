package GUI.Player;

import GUI.GeneralText;
import Interface.VerifiedEntity;
import model.Player;

import javax.swing.table.AbstractTableModel;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * The {@code PlayerTableModel} class is an implementation of {@code AbstractTableModel}
 * that represents a tabular model of player data. This class provides methods for
 * managing and displaying rows and columns of player information in a table.
 * @author SIN
 */
public class PlayerTableModel extends AbstractTableModel {
    private String[] columns_name;
    private Object[][] data;

    /**
     * Constructs a {@code PlayerTableModel} object to represent a tabular model of player data.
     * This constructor initializes the column names by retrieving options through
     * {@code GeneralText.fetch().getOptions()} and populates the table data by
     * calling {@code parse_data}.
     *
     * @param player_data a {@code TreeMap} mapping player IDs to their corresponding {@code Player} objects,
     *                    which is used to populate the table model.
     */
    public PlayerTableModel(TreeMap<Integer, VerifiedEntity> player_data) {
        columns_name = GeneralText.fetch().getOptions("table_column");
        parse_data(player_data);
    }

    /**
     * Populates the {@code data} variable with player information from the given {@code TreeMap}.
     * Each player's ID and corresponding {@code Player} object are transferred into a two-dimensional array,
     * where the first column stores the player IDs and the second column stores the {@code Player} objects.
     *
     * @param player_data a {@code TreeMap} containing player IDs as keys and their corresponding
     *                    {@code Player} objects as values. This map is used to populate the
     *                    {@code data} array for further use in the table model.
     */
    private void parse_data(TreeMap<Integer, VerifiedEntity> player_data){
        this.data = new Object[player_data.size()][2];
        int rowIndex = 0;
        Set<Map.Entry<Integer, VerifiedEntity>> entrySet = player_data.entrySet();
        for (Map.Entry<Integer, VerifiedEntity> entry : entrySet) {
            data[rowIndex][0] = entry.getKey();
            data[rowIndex][1] = entry.getValue();
            rowIndex++;
        }
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columns_name.length;
    }

    /**
     * Retrieves the value at the specified row and column in the table model.
     * The table data is derived from {@code Player} objects which are mapped to rows.
     * Columns correspond to specific attributes of the {@code Player} object.
     *
     * @param rowIndex the index of the row whose data is to be retrieved
     * @param columnIndex the index of the column representing the attribute to retrieve.
     *                    Valid indices are:
     *                    <ul>
     *                    <li>0 - retrieves the player's ID using {@code Player.getID()}</li>
     *                    <li>1 - retrieves the player's region using {@code Player.getRegion()}</li>
     *                    <li>2 - retrieves the player's server using {@code Player.getServer()}</li>
     *                    <li>3 - retrieves the player's name using {@code Player.getName()}</li>
     *                    </ul>
     * @return the value at the specified row and column. The returned object can be an Integer,
     *         String, or any type depending on the column being accessed.
     * @throws IllegalStateException if the columnIndex is invalid or does not match one of the expected column mappings.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Player player = (Player) data[rowIndex][1];
        return switch (columnIndex){
            case 0 -> player.getID();
            case 1 -> player.getRegion();
            case 2 -> player.getServer();
            case 3 -> player.getName();
            default -> throw new IllegalStateException("Unexpected value reading player column data ");
        };
    }

    @Override
    public String getColumnName(int column) {
        return columns_name[column];
    }

    /**
     * Updates the underlying data structure of the table model with new player information
     * and notifies all listeners that the data has changed. This method invokes {@code parse_data}
     * to process the provided player data and then triggers {@code fireTableDataChanged}
     * to refresh the table view.
     *
     * @param player_data a {@code TreeMap} containing player IDs as keys and their corresponding
     *                    {@code Player} objects as values. This map is used to update the
     *                    table model data for display.
     */
    public void update_data(TreeMap<Integer, VerifiedEntity> player_data) {
        parse_data(player_data);
        fireTableDataChanged();
    }

    /**
     * Updates the column names of the table model based on the current language settings
     * and refreshes the table structure to reflect the changes. This method is typically
     * used as a callback when the language setting changes, ensuring that the table's
     * column names are displayed in the appropriate language.
     * <p>
     * The method retrieves the updated column names by invoking
     * {@code GeneralText.fetch().getOptions("table_column")} and assigns the
     * resulting array to {@code columns_name}. It then calls {@code fireTableStructureChanged()}
     * to notify all listeners that the table structure has been updated.
     */
    public void language_changed() {
        columns_name = GeneralText.fetch().getOptions("table_column");
        fireTableStructureChanged();
    }

}
