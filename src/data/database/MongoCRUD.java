package data.database;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import data.DataOperation;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.DataInfo;
import org.bson.Document;

import java.util.TreeMap;

/**
 * @author SIN
 */
@SuppressWarnings("unused")
public class MongoCRUD implements GeneralCRUD<DataInfo> {
    private final DataInfo dataInfo;
    MongoClient mongoClient;
    MongoCollection<Document> collection;

    public MongoCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() {
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
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try(MongoCursor<Document> cursor = switch (dataOperation){
            case READ -> collection.find().iterator();
            case SEARCH -> collection.find(Filters.eq("id",((TreeMap<?, ?>) dataContainer).firstKey())).iterator();
            default -> throw new OperationException("Unexpected DataOperation for reading: " + dataOperation);
        }){
            cursor.forEachRemaining(document -> parser.parse((R)document, dataOperation, dataContainer));
        }catch(Exception e){
            throw new DatabaseException("Can not read data or data not found! Cause: " + e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        Document document = new Document();
        parser.parse((R)document, dataOperation, dataContainer);
        try {
            switch(dataOperation){
                case ADD -> collection.insertOne(document);
                case MODIFY -> collection.updateOne(new Document("id", document.get("id")), new Document("$set", document));
                case DELETE -> collection.deleteOne(document);
            }
        } catch (Exception e) {
            throw new DatabaseException("Can not update data. Caused: " + e.getMessage());
        }
        return this;
    }

}
