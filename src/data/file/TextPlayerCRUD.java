package data.file;

import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author SIN
 */
public class TextPlayerCRUD implements PlayerCRUD<DataInfo> {
    File file;
    DataInfo dataInfo;

    @Override
    public PlayerCRUD<DataInfo> prepare(DataInfo dataInfo) {
        file = new File(dataInfo.getUrl());
        if (file.exists() && file.canRead() && file.canWrite()){
            this.dataInfo = dataInfo;
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
        ArrayList<String> list = new ArrayList<>();
        try(Scanner scanner = new Scanner(file)){
            while(scanner.hasNext()){
                list.add(scanner.next());
            }
            parser.parse((R)list, operation, dataMap);
        }catch (Exception e) {
            throw new FileManageException("Error reading this txt file");
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> PlayerCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        ArrayList<String> list = new ArrayList<>();
        parser.parse((R)list, operation, object);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file,false))){
            for(String string : list){
                bw.write(string);
            }
        }catch (IOException e){
            throw new FileManageException(e.getMessage());
        }
        return this;
    }

}
