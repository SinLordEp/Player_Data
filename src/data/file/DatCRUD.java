package data.file;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author SIN
 */
@SuppressWarnings("unused")
public class DatCRUD implements GeneralCRUD<DataInfo> {
    private final DataInfo dataInfo;
    private Path path;

    public DatCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() {
        path = Paths.get(dataInfo.getUrl());
        if(Files.exists(path) && Files.isReadable(path) && Files.isWritable(path)){
            return this;
        }else{
            throw new FileManageException("File cannot be read or write");
        }
    }

    @Override
    public void release() {
        path = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(path))) {
            if (Files.size(path) == 0) {
                return this;
            }
            while (true) {
                Object tempObject = ois.readObject();
                if("EOF".equals(tempObject)){
                    break;
                }
                if(!(tempObject instanceof VerifiedEntity)){
                    throw new FileManageException("Object is not VerifiedEntity");
                }else{
                    parser.parse((R) tempObject, dataOperation, dataContainer);
                }
            }
        }catch (Exception e){
            throw new FileManageException(e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try(ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            ArrayList<VerifiedEntity> verifiedEntities = new ArrayList<>();
            parser.parse((R)verifiedEntities, null, dataContainer);
            verifiedEntities.forEach(verifiedEntity -> {
                try {
                    oos.writeObject(verifiedEntity);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            oos.writeObject("EOF");
        }catch (IOException e){
            throw new FileManageException(e.getMessage());
        }
        return this;
    }

}
