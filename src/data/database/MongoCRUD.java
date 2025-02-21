package data.database;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import data.DataOperation;
import exceptions.OperationException;
import model.DataInfo;
import org.bson.Document;

import java.util.TreeMap;

/**
 * @author SIN
 */
public class MongoCRUD implements GeneralCRUD<DataInfo> {
    DataInfo dataInfo;
    MongoClient mongoClient;
    MongoCollection<Document> collection;
    @Override
    public GeneralCRUD<DataInfo> prepare(DataInfo dataInfo) {
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
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        try(MongoCursor<Document> cursor = switch (operation){
            case READ -> collection.find().iterator();
            case SEARCH -> collection.find(Filters.eq("id",((TreeMap<?, ?>) dataMap).firstKey())).iterator();
            default -> throw new OperationException("Unexpected DataOperation for reading: " + operation);
        }){
            ((TreeMap<?, ?>) dataMap).clear();
            while(cursor.hasNext()){
                Document document = cursor.next();
                parser.parse((R)document, operation, dataMap);
            }
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
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
