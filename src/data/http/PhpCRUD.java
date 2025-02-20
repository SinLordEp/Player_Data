package data.http;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import data.DataOperation;
import exceptions.DataCorruptedException;
import exceptions.HttpPhpException;
import model.DataInfo;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class PhpCRUD implements GeneralCRUD<DataInfo> {
    ApiRequests api;
    private String url;
    private String readUrl;
    private String writeUrl;
    private String searchUrl;
    DataInfo dataInfo;

    @Override
    public GeneralCRUD<DataInfo> prepare(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
        if(dataInfo.getDataType() == PhpType.JSON){
            api = new ApiRequests();
            url = getProperty("phpURL");
            readUrl = getProperty("phpReadURL");
            writeUrl = getProperty("phpWriteURL");
            searchUrl = getProperty("phpSearchURL");
            return this;
        }
        throw new HttpPhpException("Invalid php type");
    }

    @Override
    public void release() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> search(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", ((TreeMap<?,?>) dataMap).firstKey());
        try {
            String postRequest = api.postRequest(url + searchUrl, jsonObject.toJSONString());
            JSONObject parsedJson = (JSONObject) JSONValue.parse(postRequest);
            if(parsedJson == null) {
                throw new DataCorruptedException("Data is null");
            }
            if("error".equals(parsedJson.get("status").toString())) {
                throw new HttpPhpException(parsedJson.get("message").toString());
            }
            parser.parse((R)parsedJson, operation, dataMap);
        } catch (IOException e) {
            throw new HttpPhpException(e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        try {
            String rawJson = api.getRequest(url + readUrl);
            JSONObject parsedJson = (JSONObject) JSONValue.parse(rawJson);
            if(parsedJson == null) {
                throw new DataCorruptedException("Data is null");
            }
            if("error".equals(parsedJson.get("status").toString())) {
                throw new HttpPhpException(parsedJson.get("message").toString());
            }
            parser.parse((R)parsedJson, operation, dataMap);
        } catch (IOException e) {
            throw new HttpPhpException(e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        JSONObject jsonObject = new JSONObject();
        try {
            parser.parse((R) jsonObject, operation, object);
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
