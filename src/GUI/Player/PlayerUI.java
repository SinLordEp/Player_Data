package GUI.Player;

import model.Player;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;

public class PlayerUI {
    private JTable Table_data;
    private JButton Button_show;
    private JButton Button_modify;
    private JButton Button_export;
    private JScrollPane Scroll_data;
    private JTextField Field_search;
    private JLabel Label_search;
    private JPanel main_panel;
    private PlayerTableModel tableModel;

    public PlayerUI(TreeMap<Integer, Player> player_data) throws HeadlessException {
        tableModel = new PlayerTableModel(player_data);
        Table_data.setModel(tableModel);
        Field_search.getDocument().addDocumentListener(new SearchListener() {
            @Override
            public void update() {
                String searchKey = Field_search.getText();
                if (!searchKey.isEmpty()) {
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        if (tableModel.getValueAt(i, 0).toString().contains(searchKey)) {
                            Table_data.setRowSelectionInterval(i, i);
                            Table_data.scrollRectToVisible(Table_data.getCellRect(i, 0, true));
                            break;
                        }
                    }
                } else {
                    Table_data.clearSelection();
                }
            }
        });
    }

    public JPanel getMain_panel() {
        return main_panel;
    }
}


