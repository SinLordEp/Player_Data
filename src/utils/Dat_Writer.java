package utils;

import model.Person;

import java.io.*;
import java.util.Map;

public class Dat_Writer implements File_Manager{
    private final ObjectOutputStream oos;
    public Dat_Writer(File file) throws IOException {
        this.oos = new ObjectOutputStream(new FileOutputStream(file,false));
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
