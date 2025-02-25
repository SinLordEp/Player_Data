package data.file;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;

import java.io.*;
import java.util.ArrayList;

/**
 * @author SIN
 */
@SuppressWarnings("unused")
public class DatCRUD implements GeneralCRUD<DataInfo> {
    private final DataInfo dataInfo;
    private File file;

    public DatCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() {
        file = new File(dataInfo.getUrl());
        if (file.exists() && file.canRead() && file.canWrite()){
            return this;
        }else{
            throw new FileManageException("File cannot be read or write");
        }
    }

    @Override
    public void release() {
        file = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        if (file.length() == 0) {
            return this;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                Object temp = ois.readObject();
                if("EOF".equals(temp)){
                    break;
                }
                if(!(temp instanceof VerifiedEntity)){
                    throw new FileManageException("Object is not VerifiedEntity");
                }else{
                    parser.parse((R)temp, operation, dataMap);
                }
            }
        }catch (Exception e){
            throw new FileManageException(e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file,false))){
            ArrayList<VerifiedEntity> verifiedEntities = new ArrayList<>();
            parser.parse((R)verifiedEntities, null, object);
            for(VerifiedEntity verifiedEntity : verifiedEntities){
                oos.writeObject(verifiedEntity);
            }
            oos.writeObject("EOF");
        }catch (IOException e){
            throw new FileManageException(e.getMessage());
        }
        return this;
    }

}
