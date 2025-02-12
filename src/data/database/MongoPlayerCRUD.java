package data.database;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import data.DataOperation;
import model.DataInfo;
import org.bson.Document;

/**
 * @author SIN
 */
public class MongoPlayerCRUD implements PlayerCRUD<DataInfo> {
    DataInfo dataInfo;
    MongoClient mongoClient;
    MongoCollection<Document> collection;
    @Override
    public PlayerCRUD<DataInfo> prepare(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
        mongoClient = new MongoClient(dataInfo.getUrl(), Integer.parseInt(dataInfo.getPort()));
        MongoDatabase database = mongoClient.getDatabase(dataInfo.getDatabase());
        collection = database.getCollection(dataInfo.getTable());
        return this;
    }

    @Override
    public void release() {
        mongoClient.close();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        try(MongoCursor<Document> cursor = collection.find().iterator()){
            while(cursor.hasNext()){
                Document document = cursor.next();
                parser.parse((R)document, operation, dataMap);
            }
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        Document document = new Document();
        parser.parse((R)document, operation, object);
        switch(operation){
            case ADD -> collection.insertOne(document);
            case MODIFY -> collection.updateOne(new Document("id", document.get("id")), new Document("$set", document));
            case DELETE -> collection.deleteOne(document);
        }
        return this;
    }

}
