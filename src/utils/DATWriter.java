package utils;

import model.Player;
import model.PlayerOperationData;

import java.io.*;
import java.util.Map;

public class DATWriter implements FileWriter{
    private final ObjectOutputStream oos;
    private final Map<Integer, Player> player_data;

    public DATWriter(PlayerOperationData current_data) throws IOException {
        this.oos = new ObjectOutputStream(new FileOutputStream(current_data.getFile(),false));
        this.player_data = current_data.getPlayer_data();
    }

    @Override
    public void write_player() {
        try{
            for(Player player : player_data.values()){
                oos.writeObject(player);
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
