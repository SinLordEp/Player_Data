package data.database;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
import data.DataOperation;
import data.PlayerCRUDFactory;
import data.file.FileType;
import exceptions.DatabaseException;
import model.DataInfo;
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

public class BaseXPlayerCRUD implements PlayerCRUD<DataInfo> {
    Context context = new Context();
    DataInfo dataInfo;

    @Override
    public PlayerCRUD<DataInfo> prepare(DataInfo dataInfo) {
        try {
            new Open(dataInfo.getDatabase()).execute(context);
            this.dataInfo = dataInfo;
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
    public <R, U> PlayerCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        try {
            String result =  new XQuery(dataInfo.getDatabase()).execute(context);
            PlayerCRUDFactory.getInstance()
                    .getCRUD(FileType.XML)
                    .prepare(result)
                    .read();
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
        return null;
    }

    @Override
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        return null;
    }

    @Override
    public PlayerCRUD<DataInfo> read(ParserCallBack<DataInfo> data) {
        String query = "/Player";
        try {
            String result =  new XQuery(query).execute(context);
            PlayerCRUDFactory.getInstance()
                    .getCRUD(FileType.XML)
                    .prepare(result)
                    .read(player_map);
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public PlayerCRUD<DataInfo> update(HashMap<Player, DataOperation> changed_player_map) {
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
    public PlayerCRUD<DataInfo> export(ParserCallBack<R> parser, TreeMap<Integer, VerifiedEntity> playerMap) {
        try {
            new CreateDB(dataInfo.getDatabase(), dataInfo.getUrl()).execute(context);
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
