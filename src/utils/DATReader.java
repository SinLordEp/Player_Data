package utils;

import model.PlayerOperationData;
import model.Player;

import java.io.*;
import java.util.HashMap;

public class DATReader implements FileReader {
    private final ObjectInputStream ois;

    public DATReader(PlayerOperationData current_data) throws IOException {
        File file = current_data.getFile();
        this.ois = new ObjectInputStream(new FileInputStream(file));
    }

    @Override
    public HashMap<Integer, Player> parse_player() throws IOException, ClassNotFoundException {
        HashMap<Integer, Player> person = new HashMap<>();
        try{
            //.readObject will always throw EOFException when reach the end of file
            while(true){
                Player temp = (Player) ois.readObject();
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
