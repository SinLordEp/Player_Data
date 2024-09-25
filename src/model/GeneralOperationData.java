package model;

import utils.DataReader;
import utils.DataWriter;

import java.io.File;


public abstract class GeneralOperationData {
    protected File file = null;
    protected boolean file_changed = false;
    protected String file_extension = "";
    protected DataReader reader;
    protected DataWriter writer;

    public DataWriter getWriter() {
        return writer;
    }

    public void setWriter(DataWriter writer) {
        this.writer = writer;
    }

    public DataReader getReader() {
        return reader;
    }

    public void setReader(DataReader reader) {
        this.reader = reader;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isFile_changed() {
        return file_changed;
    }

    public void setFile_changed(boolean file_changed) {
        this.file_changed = file_changed;
    }

    public String getFile_extension() {
        return file_extension;
    }

    public void setFile_extension(String file_extension) {
        this.file_extension = file_extension;
    }

    public String getAbsolutePath(){
        return file.getAbsolutePath();
    }
}
