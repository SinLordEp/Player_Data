package utils;

import model.Person;

import java.io.*;
import java.util.HashMap;

public class Dat_Reader implements File_Manager {
    private final ObjectInputStream ois;

    public Dat_Reader(File file) throws IOException {
        this.ois = new ObjectInputStream(new FileInputStream(file));
    }

    public HashMap<Integer, Person> parse_person() throws IOException, ClassNotFoundException {
        HashMap<Integer, Person> person = new HashMap<>();
        try{
            //readObject will always throw EOFException when reach the end of file
            while(true){
                Person temp = (Person) ois.readObject();
                person.put(temp.getID(), temp);
            }
        }catch (EOFException _){
        }finally {
            close();
        }
        return person;
    }

    public void close(){
        try{
            ois.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
