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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author SIN
 */

public class BaseXPlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    Context context = new Context();
    DatabaseInfo databaseInfo;

    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) {
        try {
            this.databaseInfo = databaseInfo;
            new Open(databaseInfo.getDatabase()).execute(context);
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void release() {
        context.close();
    }

    @Override
    public PlayerCRUD<DatabaseInfo> read(TreeMap<Integer, Player> player_map) {
        String query = "/Player";
        try {
            String result =  new XQuery(query).execute(context);
            PlayerCRUDFactory.getInstance()
                    .getCRUD(FileType.XML)
                    .prepare(result)
                    .read(player_map);
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException("Error communicating with ObjectDB. \nCause: " + e.getMessage());
        }
    }

    @Override
    public PlayerCRUD<DatabaseInfo> update(HashMap<Player, DataOperation> changed_player_map) {
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
            }
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public PlayerCRUD<DatabaseInfo> export(TreeMap<Integer, Player> playerMap) {
        try {
            new CreateDB(databaseInfo.getDatabase(), databaseInfo.getUrl()).execute(context);
            for(Player player : playerMap.values()) {
                String query = "insert node <player id='%s'><region>%s</region><server>%s</server><name>%s</name></player> into /Player"
                        .formatted(player.getID(), player.getRegion(), player.getServer(), player.getName());
                new XQuery(query).execute(context);
            }
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

}
