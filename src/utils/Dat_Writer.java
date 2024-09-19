package utils;

import model.Person;

import java.io.*;
import java.util.HashMap;

public class Dat_Writer implements File_Manager{
    private final ObjectOutputStream oos;
    public Dat_Writer(File file) throws IOException {
        this.oos = new ObjectOutputStream(new FileOutputStream(file,false));
    }
    public void write_data(HashMap<Integer, Person> person) throws IOException {
        for(Person temp : person.values()){
            oos.writeObject(temp);
        }
    }
}
