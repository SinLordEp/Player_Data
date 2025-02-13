package data.file;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

/**
 * @author SIN
 */
public class XmlPlayerCRUD implements PlayerCRUD<DataInfo> {
    File file;
    String stringXML;
    boolean parseRawXML = false;
    DataInfo dataInfo;

    @Override
    public PlayerCRUD<DataInfo> prepare(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
        if(dataInfo.getUrl().startsWith("<Player>")){
            stringXML = dataInfo.getUrl();
            parseRawXML = true;
            return this;
        }else{
            file = new File(dataInfo.getUrl());
            if (file.exists() && file.canRead() && file.canWrite()){
                return this;
            }else{
                throw new FileManageException("File cannot be read or write");
            }
        }
    }



    @Override
    public void release() {
        file = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        Element element;
        try {
            if(parseRawXML){
                element = xml_utils.parseStringXml(stringXML);
            }else{
                element = xml_utils.readXml(file);
            }
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
        if (!element.hasChildNodes()) {
            return this;
        }
        parser.parse((R) element, null, dataMap);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        try {
            Document document = xml_utils.createDocument();
            parser.parse((R)document,null, object);
            xml_utils.writeXml(document, file);
        } catch (Exception e) {
            throw new FileManageException(e.getMessage());
        }
        return this;
    }

}
