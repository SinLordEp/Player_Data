package data.database;

import Interface.PlayerCRUD;
import data.DataOperation;
import data.PlayerCRUDFactory;
import data.file.FileType;
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
import java.util.TreeMap;

/**
 * @author SIN
 */

public class BaseXPlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    private static final Logger logger = LoggerFactory.getLogger(BaseXPlayerCRUD.class);
    Context context = new Context();
    DatabaseInfo databaseInfo;

    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) {
        try {
            this.databaseInfo = databaseInfo;
            new CreateDB(databaseInfo.getDatabase(), databaseInfo.getUrl()).execute(context);
            new Open(databaseInfo.getDatabase()).execute(context);
            return this;
        } catch (BaseXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void release() {
        context.close();
    }

    @Override
    public TreeMap<Integer, Player> read() throws Exception {
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

    //TODO:
    @Override
    public void update(HashMap<Player, DataOperation> changed_player_map) {
        String queryInsert = "insert node <player id='10'><region>Asia</region><server>Liyue Harbor</server><name>New Player</name></player> into /Player";
        String queryReplaceNode = "replace node /Player/player[@id='1'] with <player id='1'><region>Europe</region><server>Springvale</server><name>Updated Player</name></player>";
        String queryDelete = "delete node /Player/player[@id='1']";
    }

    @Override
    public void export(TreeMap<Integer, Player> playerMap) {
        PlayerCRUDFactory.getInstance()
                .getCRUD(FileType.XML)
                .prepare(databaseInfo.getUrl())
                .export(playerMap);
    }

}
