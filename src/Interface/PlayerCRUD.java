package Interface;

import data.DataOperation;
import model.Player;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author SIN
 */
public interface PlayerCRUD<T> {
    PlayerCRUD<T> prepare(T input);
    void release();
    TreeMap<Integer, Player> read();
    void update(HashMap<Player, DataOperation> changed_player_map);
    void export(TreeMap<Integer, Player> player_map);
}
