package data.http;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import data.DataOperation;
import exceptions.DataCorruptedException;
import exceptions.HttpPhpException;
import exceptions.OperationException;
import model.DataInfo;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.TreeMap;

/**
 * @author SIN
 */
@SuppressWarnings("unused")
public class JsonCRUD implements GeneralCRUD<DataInfo> {
    ApiRequests api;
    private String url;
    private String readUrl;
    private String writeUrl;
    private String searchUrl;
    private final DataInfo dataInfo;

    public JsonCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() {
        if(dataInfo.getDataType() == PhpType.JSON){
            api = new ApiRequests();
            url = dataInfo.getUrl();
            readUrl = dataInfo.getQueryRead();
            writeUrl = dataInfo.getQueryModify();
            searchUrl = dataInfo.getQuerySearch();
            return this;
        }
        throw new HttpPhpException("Invalid php type");
    }

    @Override
    public void release() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try {
            String response;
            switch (dataOperation){
                case SEARCH: JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", ((TreeMap<?,?>) dataContainer).firstKey());
                    response = api.postRequest(url + searchUrl, jsonObject.toJSONString());
                    break;
                case READ: response = api.getRequest(url + readUrl);
                break;
                default: throw new OperationException("Unexpected DataOperation for reading: " + dataOperation);
            }
            JSONObject parsedJson = (JSONObject) JSONValue.parse(response);
            if(parsedJson == null) {
                throw new DataCorruptedException("Data is null");
            }
            if("error".equals(parsedJson.get("status").toString())) {
                throw new HttpPhpException(parsedJson.get("message").toString());
            }
            parser.parse((R)parsedJson, dataOperation, dataContainer);
        } catch (IOException e) {
            throw new HttpPhpException(e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        JSONObject jsonObject = new JSONObject();
        try {
            parser.parse((R) jsonObject, dataOperation, dataContainer);
            String postRequest = api.postRequest(url + writeUrl, jsonObject.toJSONString());
            JSONObject response = (JSONObject) JSONValue.parse(postRequest);
            if(response == null) {
                throw new HttpPhpException("Json was sent to server but did not receive a correct response");
            }
            if("error".equals(response.get("status").toString())) {
                throw new HttpPhpException(response.get("message").toString());
            }
        } catch (IOException e) {
            throw new HttpPhpException(e.getMessage());
        }
        return this;
    }

}
