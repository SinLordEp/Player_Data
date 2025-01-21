package data.file;

import GUI.Player.PlayerText;
import Interface.PlayerFDA;
import exceptions.FileManageException;
import model.Player;

import javax.management.openmbean.OpenDataException;
import java.io.*;
import java.util.TreeMap;

/**
 * @author SIN
 */

public class DatFDA implements PlayerFDA {

    /**
     * Reads and parses a DAT file to extract serialized {@code Player} objects, storing them in a {@code TreeMap}.
     * If the file is empty, a popup notification is displayed using {@code PlayerText.getDialog().popup(String)}.
     * Throws an exception if the file data is corrupted or if an error occurs during reading.
     *
     * @param file The {@code File} object representing the DAT file to be read.
     * @return A {@code TreeMap<Integer, Player>} where the key is the player ID and the value is the corresponding {@code Player} object.
     *         Returns an empty map if the file is empty or nothing is successfully parsed.
     * @throws FileManageException if a read error occurs or if the data is corrupted.
     */
    @Override
    public TreeMap<Integer, Player> read(File file) {
        TreeMap<Integer, Player> player_data = new TreeMap<>();
        if (file.length() == 0) {
            PlayerText.getDialog().popup("player_map_null");
            return player_data;
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
                    player_data.put(player.getID(), player);
                }
            }
        }catch (Exception e){
            throw new FileManageException("Failed to read DAT file. Cause: " + e.getMessage());
        }
        if (player_data.isEmpty()) {
            PlayerText.getDialog().popup("player_map_null");
        }
        return player_data;
    }

    /**
     * Writes the provided player data to the specified file in DAT format. Each {@code Player} object
     * in the {@code player_data} map is serialized using an {@code ObjectOutputStream}. The method
     * appends an "EOF" marker at the end of the file to signify the end of content.
     *
     * @param file the file where the player data will be written. The file is overwritten if it already exists.
     * @param player_map a {@code TreeMap<Integer, Player>} containing the player data to be serialized and written to the file.
     *                    If the map is null or empty, only the "EOF" marker will be written.
     * @throws FileManageException if an error occurs during file writing, such as an {@code IOException}.
     */
    @Override
    public void write(File file, TreeMap<Integer, Player> player_map) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file,false))){
            if(player_map != null) {
                for (Player player : player_map.values()) {
                    oos.writeObject(player);
                }
            }
            oos.writeObject("EOF");
        }catch (IOException e){
            throw new FileManageException("Failed to write player data via DAT. Cause: " + e.getMessage());
        }
    }
}
