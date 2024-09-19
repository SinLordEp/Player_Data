package utils;

import model.Person;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Dat_Reader implements File_Manager {
    private final ObjectInputStream ois;
    public Dat_Reader(File file) throws IOException {
        this.ois = new ObjectInputStream(new FileInputStream(file));
    }
    public HashMap<Integer, Person> read_person() throws IOException, ClassNotFoundException {
        HashMap<Integer, Person> person = new HashMap<>();
        while(ois.available()>0){
            Person temp = (Person) ois.readObject();
            person.put(temp.getID(), temp);
        }
        return person;
    }
}
