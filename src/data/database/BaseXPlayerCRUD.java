package data.database;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import data.DataOperation;
import data.PlayerCRUDFactory;
import data.file.FileType;
import exceptions.DatabaseException;
import model.DataInfo;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;

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
            DataInfo tempDataInfo = new DataInfo();
            tempDataInfo.setDataType(FileType.XML);
            tempDataInfo.setUrl(result);
            PlayerCRUDFactory.getInstance()
                    .getCRUD(tempDataInfo)
                    .prepare(tempDataInfo)
                    .read(parser, operation, dataMap)
                    .release();
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        try {
            String query = "";
            parser.parse((R)query, operation, object);
            new XQuery(query).execute(context);
            return this;
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

}
