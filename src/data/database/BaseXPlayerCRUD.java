package data.database;

import Interface.PlayerCRUD;
import data.DataOperation;
import data.PlayerCRUDFactory;
import data.file.FileType;
import exceptions.DatabaseException;
import model.DatabaseInfo;
import model.Player;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author SIN
 */

public class BaseXPlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    private final Logger logger = LoggerFactory.getLogger(BaseXPlayerCRUD.class);
    Context context = new Context();
    DatabaseInfo databaseInfo;

    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) {
        try {
            this.databaseInfo = databaseInfo;
            new Open(databaseInfo.getDatabase()).execute(context);
            return this;
        } catch (BaseXException e) {
            logger.error("Failed to prepare BaseX. Cause: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void release() {
        context.close();
    }

    @Override
    public TreeMap<Integer, Player> read() {
        String query = "/Player";
        try {
            String result =  new XQuery(query).execute(context);
            return PlayerCRUDFactory.getInstance()
                    .getCRUD(FileType.XML)
                    .prepare(result)
                    .read();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            release();
        }
    }

    @Override
    public void update(HashMap<Player, DataOperation> changed_player_map) {
        try {
            for(Map.Entry<Player, DataOperation> player_operation : changed_player_map.entrySet()) {
                Player player = player_operation.getKey();
                String query = switch (player_operation.getValue()){
                    case ADD -> "insert node <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player> into /Player"
                            .formatted(player.getID(), player.getRegion(), player.getServer(), player.getName());
                    case MODIFY -> "replace node /Player/player[@id='%s'] with <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player>"
                            .formatted(player.getID(), player.getID(), player.getRegion(), player.getServer(), player.getName());
                    case DELETE -> "delete node /Player/player[@id='%s']".formatted(player.getID());
                };
                new XQuery(query).execute(context);
                logger.info("Player {} has been updated", player.getID());
            }
        } catch (BaseXException e) {
            throw new DatabaseException("Failed to update BaseX. Cause: " + e.getMessage());
        }

    }

    @Override
    public void export(TreeMap<Integer, Player> playerMap) {
        try {
            new CreateDB(databaseInfo.getDatabase(), databaseInfo.getUrl()).execute(context);
            for(Player player : playerMap.values()) {
                String query = "insert node <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player> into /Player"
                        .formatted(player.getID(), player.getRegion(), player.getServer(), player.getName());
                new XQuery(query).execute(context);
            }
        } catch (BaseXException e) {
            throw new DatabaseException("Failed to export BaseX. Cause: " + e.getMessage());
        }
    }

}
