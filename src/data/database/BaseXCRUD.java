package data.database;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import data.CRUDFactory;
import data.DataOperation;
import data.file.FileType;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.DataInfo;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.cmd.Open;
import org.basex.core.cmd.XQuery;

import java.util.TreeMap;

/**
 * @author SIN
 */

@SuppressWarnings("unused")
public class BaseXCRUD implements GeneralCRUD<DataInfo> {
    private final Context context = new Context();
    private final DataInfo dataInfo;

    public BaseXCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() {
        try {
            new Open(dataInfo.getDatabase()).execute(context);
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
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try {
            String result = switch (dataOperation){
                case READ -> new XQuery(dataInfo.getDatabase()).execute(context);
                case SEARCH -> new XQuery(dataInfo.getQuerySearch().formatted(String.valueOf(((TreeMap<?, ?>) dataContainer).firstKey()))).execute(context);
                default -> throw new OperationException("Unexpected DataOperation for reading: " + dataOperation);
            };
            DataInfo tempDataInfo = new DataInfo();
            tempDataInfo.setDataType(FileType.XML);
            tempDataInfo.setUrl(result);
            CRUDFactory.getCRUD(tempDataInfo)
                    .prepare()
                    .read(parser, dataOperation, dataContainer)
                    .release();
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try {
            String[] query = new String[1];
            parser.parse((R)query, dataOperation, dataContainer);
            new XQuery(query[0]).execute(context);
        } catch (BaseXException e) {
            throw new DatabaseException(e.getMessage());
        }
        return this;
    }

}
