package Interface;

import model.Player;

import java.io.File;
import java.util.TreeMap;

/**
 * @author SIN
 */

public interface PlayerFDA {
    TreeMap<Integer, Player> read(File file);
    void write(File file, TreeMap<Integer, Player> player_map);
}
