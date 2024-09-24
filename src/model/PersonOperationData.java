package model;

import javax.swing.*;
import java.util.Map;

public class PersonOperationData extends GeneralOperationData{
    private Map<Integer, Person> person_data = null;
    private Map<String, String[]> region_server = null;
    private String person_type = "";

    public PersonOperationData() {
        super.setData_class("Person");
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

    public String getPerson_type() {
        return person_type;
    }

    public void setPerson_type(String person_type) {
        this.person_type = person_type;
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

    public String[] getServer(String region){
        return region_server.get(region);
    }

    public void print_person(){
        for(Person temp : person_data.values()){
            JOptionPane.showMessageDialog(null, temp.toString());
        }
    }
}
