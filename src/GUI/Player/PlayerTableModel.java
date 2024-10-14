package GUI.Player;

import model.Player;

import javax.swing.table.AbstractTableModel;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class PlayerTableModel extends AbstractTableModel {
    private final String[] columns_name = {"id_player","region","server","name"};
    private TreeMap<Integer, Player> player_data;
    private final Object[][] data;

    public PlayerTableModel(TreeMap<Integer, Player> player_data) {
        this.player_data = player_data;
        this.data = new Object[player_data.size()][2];
        int rowIndex = 0;
        Set<Map.Entry<Integer, Player>> entrySet = player_data.entrySet();
        for (Map.Entry<Integer, Player> entry : entrySet) {
            data[rowIndex][0] = entry.getKey();
            data[rowIndex][1] = entry.getValue();
            rowIndex++;
        }
    }

    @Override
    public int getRowCount() {
        return player_data.size();
    }

    @Override
    public int getColumnCount() {
        return columns_name.length;
    }

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

    public void update_data(TreeMap<Integer, Player> player_data) {
        this.player_data = player_data;
        fireTableDataChanged();
    }


}
