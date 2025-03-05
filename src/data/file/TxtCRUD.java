package data.file;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import data.DataOperation;
import exceptions.FileManageException;
import model.DataInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author SIN
 */
@SuppressWarnings("unused")
public class TxtCRUD implements GeneralCRUD<DataInfo> {
    private Path path;
    private final DataInfo dataInfo;

    public TxtCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() {
        path = Paths.get(dataInfo.getUrl());
        if(Files.exists(path) && Files.isRegularFile(path) && Files.isWritable(path)) {
            return this;
        } else{
            throw new FileManageException("File does not exist or cannot be read/write");
        }
    }

    @Override
    public void release() {
        path = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> read(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try(Stream<String> stream = Files.lines(path)) {
            stream.forEach(line -> parser.parse((R) line, dataOperation, dataContainer));
        } catch (IOException e) {
            throw new FileManageException("Error reading this txt file.>>>" + e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        try {
            List<String> lines = new ArrayList<>();
            parser.parse((R)lines, dataOperation, dataContainer);
            Files.write(path, lines);
        }catch (IOException e){
            throw new FileManageException("Error writing this txt file.>>>" + e.getMessage());
        }
        return this;
    }

}
