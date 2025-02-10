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
    PlayerCRUD<T> read(TreeMap<Integer, Player> player_map);
    PlayerCRUD<T> update(HashMap<Player, DataOperation> changed_player_map);
    PlayerCRUD<T> export(TreeMap<Integer, Player> player_map);
}
