package utils;

import model.PersonOperationData;
import model.Person;

import java.io.*;
import java.util.HashMap;

public class DATReader implements FileReader<Object> {
    private final ObjectInputStream ois;
    private final String data_type;

    public DATReader(PersonOperationData current_data) throws IOException {
        File file = current_data.getFile();
        this.data_type = current_data.getPerson_type();
        this.ois = new ObjectInputStream(new FileInputStream(file));
    }

    @Override
    public Object read () throws Exception {
        if()
        return switch (data_type){
            case "Player" -> parse_player();
            case "GM" -> throw new Exception("Type is not available yet");
            default -> throw new IllegalStateException("Unexpected data type");
        };

    }

    public Object read_person() throws Exception {
        return switch (data_type){
            case "Player" -> parse_player();
            case "GM" -> throw new Exception("Type is not available yet");
            default -> throw new IllegalStateException("Unexpected data type");
        };
    }
    @Override
    public HashMap<Integer, Person> parse_player() throws IOException, ClassNotFoundException {
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
