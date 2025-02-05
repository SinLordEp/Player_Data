package data.database;

import Interface.PlayerCRUD;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import data.DataOperation;
import model.DatabaseInfo;
import model.Player;
import model.Region;
import model.Server;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class MongoPlayerCRUD implements PlayerCRUD<DatabaseInfo> {
    Logger logger = LoggerFactory.getLogger(MongoPlayerCRUD.class);
    DatabaseInfo databaseInfo;
    MongoClient mongoClient;
    MongoCollection<Document> playerCollection;
    @Override
    public PlayerCRUD<DatabaseInfo> prepare(DatabaseInfo databaseInfo) {
        logger.info("Preparing connection to MongoDB");
        this.databaseInfo = databaseInfo;
        mongoClient = new MongoClient(databaseInfo.getUrl(), Integer.parseInt(databaseInfo.getPort()));
        MongoDatabase database = mongoClient.getDatabase(databaseInfo.getDatabase());
        playerCollection = database.getCollection("player");
        logger.info("Connection established");
        return this;
    }

    @Override
    public void release() {
        logger.info("Releasing connection of MongoDB");
        mongoClient.close();
    }

    @Override
    public TreeMap<Integer, Player> read() {
        logger.info("Reading data from MongoDB");
        TreeMap<Integer, Player> playerMap = new TreeMap<>();
        try(MongoCursor<Document> cursor = playerCollection.find().iterator()){
            while(cursor.hasNext()){
                Document document = cursor.next();
                Player player = new Player();
                player.setID(document.getInteger("id"));
                player.setName(document.getString("name"));
                player.setRegion(new Region(document.getString("region")));
                player.setServer(new Server(document.getString("server"), player.getRegion()));
                playerMap.put(player.getID(), player);
            }
        }
        release();
        return playerMap;
    }

    @Override
    public void export(TreeMap<Integer, Player> player_map) {
        logger.info("Dropping possible existed collection");
        playerCollection.drop();
        for(Player player : player_map.values()){
            Document document = new Document();
            document.put("id", player.getID());
            document.put("name", player.getName());
            document.put("region", player.getRegion().getName());
            document.put("server", player.getServer().getName());
            playerCollection.insertOne(document);
            logger.info("Player with id {} was exported", player.getID());
        }
        release();
    }

    @Override
    public void update(HashMap<Player, DataOperation> changed_player_map) {
        for(Map.Entry<Player, DataOperation> playerEntry : changed_player_map.entrySet()){
            Document document = new Document();
            Player player = playerEntry.getKey();
            switch(playerEntry.getValue()){
                case ADD: document.put("id", player.getID());
                    document.put("name", player.getName());
                    document.put("region", player.getRegion().getName());
                    document.put("server", player.getServer().getName());
                    playerCollection.insertOne(document);
                    logger.info("Player with id {} was added", player.getID());
                    break;
                case MODIFY: document.put("id", player.getID());
                    document.put("name", player.getName());
                    document.put("region", player.getRegion().getName());
                    document.put("server", player.getServer().getName());
                    playerCollection.updateOne(new Document("id",player.getID()), new Document("$set", document));
                    logger.info("Player with id {} was modified", player.getID());
                    break;
                case DELETE: document.put("id", player.getID());
                    playerCollection.deleteOne(document);
                    logger.info("Player with id {} was deleted", player.getID());
                    break;
            }
        }
        release();
    }
}
