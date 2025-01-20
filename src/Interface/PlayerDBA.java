package Interface;

import data.DataOperation;
import exceptions.DatabaseException;
import model.DatabaseInfo;
import model.Player;
import model.Region;
import model.Server;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author SIN
 */
public interface PlayerDBA {
    PlayerDBA connect(DatabaseInfo databaseInfo) throws DatabaseException;
    void disconnect();
    TreeMap<Integer, Player> read() throws Exception;
    void update(HashMap<Player, DataOperation> changed_player_map);
    void export(TreeMap<Integer, Player> player_map);
    HashMap<Region, Server[]> readRegionServer();
}
