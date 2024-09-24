package model;

import java.io.File;

public class GeneralOperationData {
    private File file = null;
    private boolean file_changed = false;
    private String file_extension = "", data_class = "";

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

    public String getData_class() {
        return data_class;
    }

    public void setData_class(String data_class) {
        this.data_class = data_class;
    }

    public String getFile_extension() {
        return file_extension;
    }

    public void setFile_extension(String file_extension) {
        this.file_extension = file_extension;
    }
    public boolean isFile_valid(){
        return file != null && file.getName().substring(file.getName().lastIndexOf('.') + 1).equals(file_extension);
    }

    public String getAbsolutePath(){
        return file.getAbsolutePath();
    }
}
