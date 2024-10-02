package GUI.Player;

import model.Player;

import javax.swing.table.AbstractTableModel;
import java.util.TreeMap;

public class PlayerTableModel extends AbstractTableModel {
    private final String[] columns_name = {"id_player","region","server","name"};
    private TreeMap<Integer, Player> player_data;

    public PlayerTableModel(TreeMap<Integer, Player> player_data) {
        this.player_data = player_data;
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
        Player player = player_data.get(rowIndex+1);
        return switch (columnIndex){
            case 1 -> player.getID();
            case 2 -> player.getRegion();
            case 3 -> player.getServer();
            case 4 -> player.getName();
            default -> throw new IllegalStateException("Unexpected value reading player column data ");
        };
    }

    @Override
    public String getColumnName(int column) {
        return columns_name[column];
    }

    public void update_data(TreeMap<Integer, Player> player_data) {
        this.player_data = player_data;
    }


}
