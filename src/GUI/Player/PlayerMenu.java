package GUI.Player;

import model.Player;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;

public class PlayerMenu extends JFrame {
    private JTable Table_data;
    private JButton Button_show;
    private JButton Button_modify;
    private JButton Button_export;
    private JScrollPane Scroll_data;
    private JTextField Field_search;
    private JLabel Label_search;
    private PlayerTableModel tableModel;

    public PlayerMenu(TreeMap<Integer, Player> player_data) throws HeadlessException {
        super("Player Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tableModel = new PlayerTableModel(player_data);
        Table_data = new JTable(tableModel);
    }




}
