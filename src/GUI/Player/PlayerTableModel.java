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
        return 0;
    }

    @Override
    public int getColumnCount() {
        return 0;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }

}
