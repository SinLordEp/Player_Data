package utils;

import model.PersonOperationData;
import model.Person;

import java.io.*;
import java.util.Map;

public class DATWriter implements FileWriter{
    private final ObjectOutputStream oos;
    private final String data_type;
    public DATWriter(PersonOperationData current_data) throws IOException {
        this.oos = new ObjectOutputStream(new FileOutputStream(current_data.getFile(),false));
        this.data_type = current_data.getPerson_type();
    }

    @Override
    public void write() {
        switch (data_type){
            case
        }
    }

    public void write_person(Map<Integer, Person> person) {
        try{
            for(Person temp : person.values()){
                oos.writeObject(temp);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            close();
        }
    }

    public void close(){
        try{
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
