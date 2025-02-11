package data.file;

import GUI.Player.PlayerText;
import Interface.ParserCallBack;
import Interface.PlayerCRUD;
import Interface.VerifiedEntity;
import data.DataOperation;
import exceptions.FileManageException;
import model.Player;

import javax.management.openmbean.OpenDataException;
import java.io.*;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author SIN
 */

public class DatPlayerCRUD implements PlayerCRUD<String> {
    File file;
    @Override
    public PlayerCRUD<String> prepare(String input) {
        file = new File(input);
        if (file.exists() && file.canRead() && file.canWrite()){
            return this;
        }else{
            throw new FileManageException("File cannot be read or write");
        }
    }

    @Override
    public void release() {
        file = null;
    }

    /**
     * Reads and parses a DAT file to extract serialized {@code Player} objects, storing them in a {@code TreeMap}.
     * If the file is empty, a popup notification is displayed using {@code PlayerText.getDialog().popup(String)}.
     * Throws an exception if the file data is corrupted or if an error occurs during reading.
     *
     * @return A {@code TreeMap<Integer, Player>} where the key is the player ID and the value is the corresponding {@code Player} object.
     *         Returns an empty map if the file is empty or nothing is successfully parsed.
     * @throws FileManageException if a read error occurs or if the data is corrupted.
     */
    @Override
    public PlayerCRUD<String> read(ParserCallBack<String> data) {
        if (file.length() == 0) {
            PlayerText.getDialog().popup("player_map_null");
            return this;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                Object temp = ois.readObject();
                if("EOF".equals(temp)){
                    break;
                }
                if(!(temp instanceof Player player)){
                    throw new OpenDataException("Data is corrupted");
                }else{
                    player_map.put(player.getID(), player);
                }
            }
        }catch (Exception e){
            throw new FileManageException(e.getMessage());
        }
        if (player_map.isEmpty()) {
            PlayerText.getDialog().popup("player_map_null");
        }
        return this;
    }

    @Override
    public PlayerCRUD<String> update(HashMap<Player, DataOperation> changed_player_map) {
        return this;
    }

    /**
     * Writes the provided player data to the specified file in DAT format. Each {@code Player} object
     * in the {@code player_data} map is serialized using an {@code ObjectOutputStream}. The method
     * appends an "EOF" marker at the end of the file to signify the end of content.
     *
     * @param parser
     * @param dataMap a {@code TreeMap<Integer, Player>} containing the player data to be serialized and written to the file.
     *                If the map is null or empty, only the "EOF" marker will be written.
     * @throws FileManageException if an error occurs during file writing, such as an {@code IOException}.
     */
    @Override
    public PlayerCRUD<String> export(ParserCallBack<R> parser, TreeMap<Integer, VerifiedEntity> dataMap) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file,false))){
            if(dataMap != null) {
                for (Player player : dataMap.values()) {
                    oos.writeObject(player);
                }
            }
            oos.writeObject("EOF");
        }catch (IOException e){
            throw new FileManageException(e.getMessage());
        }
        return this;
    }

}
