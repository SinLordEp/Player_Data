package model;

import utils.Dat_Writer;
import utils.XML_Writer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileOperationData {
    private Map<Integer, Person> person_data = null;
    private Map<String, String[]> region_server = null;
    private File person_file = null;
    private boolean file_changed = false;
    private String file_type = "";
    private String person_type = "";

    public FileOperationData() {
    }

    public Map<Integer, Person> getPerson_data() {
        return person_data;
    }

    public void setPerson_data(Map<Integer, Person> person_data) {
        this.person_data = person_data;
    }

    public Map<String, String[]> getRegion_server() {
        return region_server;
    }

    public void setRegion_server(Map<String, String[]> region_server) {
        this.region_server = region_server;
    }

    public void setPerson_file(File person_file) {
        this.person_file = person_file;
    }

    public File getPerson_file() {
        return person_file;
    }

    public boolean isFile_changed() {
        return file_changed;
    }

    public void setFile_changed(boolean file_changed) {
        this.file_changed = file_changed;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getPerson_type() {
        return person_type;
    }

    public void setPerson_type(String person_type) {
        this.person_type = person_type;
    }

    public boolean isFile_valid(){
        return person_file == null || !person_file.getName().substring(person_file.getName().lastIndexOf('.')+1).equals(file_type);
    }

    public String getAbsolutePath(){
        return person_file.getAbsolutePath();
    }
    public boolean containsKey(int ID){
        return person_data.containsKey(ID);
    }
    public Person getFrom_Map(int ID){
        return person_data.get(ID);
    }
    public void putIn_Map(int ID, Person person){
        person_data.put(ID,  person);
    }

    public void writeTo_dat() throws IOException {
        new Dat_Writer(person_file).write_person(person_data);
    }

    public void writeTo_xml(){
        new XML_Writer().update_Person(person_file, person_type, person_data);
    }
    public String[] getServer(String region){
        return region_server.get(region);
    }

    public void print_person(){
        for(Person temp : person_data.values()){
            JOptionPane.showMessageDialog(null, temp.toString());
        }
    }
}
