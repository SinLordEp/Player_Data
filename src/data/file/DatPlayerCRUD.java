package data.file;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;

import java.io.*;
import java.util.ArrayList;

/**
 * @author SIN
 */

public class DatPlayerCRUD implements PlayerCRUD<DataInfo> {
    File file;
    @Override
    public PlayerCRUD<DataInfo> prepare(DataInfo dataInfo) {
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
    public <R, U> PlayerCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
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
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file,false))){
            parser.parse(null, null, object);
            ArrayList<VerifiedEntity> verifiedEntities = (ArrayList<VerifiedEntity>) object;
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
