package data.file;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
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
public class TxtCRUD implements GeneralCRUD<DataInfo> {
    File file;
    DataInfo dataInfo;

    @Override
    public GeneralCRUD<DataInfo> prepare(DataInfo dataInfo) {
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
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation operation, U dataMap) {
        ArrayList<String> list = new ArrayList<>();
        try(Scanner scanner = new Scanner(file)){
            while(scanner.hasNext()){
                list.add(scanner.nextLine());
            }
            parser.parse((R)list, operation, dataMap);
        }catch (Exception e) {
            throw new FileManageException("Error reading this txt file");
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
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
