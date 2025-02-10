package data.file;

import GUI.Player.PlayerText;
import Interface.PlayerCRUD;
import data.DataOperation;
import exceptions.FileManageException;
import model.Player;
import model.Region;
import model.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class TextPlayerCRUD implements PlayerCRUD<String> {
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
     * Reads and parses a TXT file containing player data, converting it into a {@code TreeMap}
     * where the key is the player's ID and the value is the corresponding {@code Player} object.
     * Each line in the file is expected to contain player details in comma-separated format:
     * {@code PlayerID, RegionName, ServerName, PlayerName}.
     * <p>
     * If the file is empty, it displays a popup notification using {@code PlayerText.getDialog().popup}.
     * In case of any error, it throws a {@code FileManageException}.
     *
     * @return A {@code TreeMap<Integer, Player>} where the key is the player ID and the value is the corresponding {@code Player} object.
     *         Returns an empty map if the file contains no player data.
     * @throws FileManageException if there is an error during file reading.
     */
    @Override
    public PlayerCRUD<String> read(TreeMap<Integer, Player> player_map) {
        try(Scanner scanner = new Scanner(file)){
            if(!scanner.hasNext()){
                PlayerText.getDialog().popup("player_map_null");
            }else{
                while(scanner.hasNext()){
                    String[] player_txt = scanner.nextLine().split(",");
                    Player player = new Player();
                    player.setID(Integer.parseInt(player_txt[0]));
                    player.setRegion(new Region(player_txt[1]));
                    player.setServer(new Server(player_txt[2], player.getRegion()));
                    player.setName(player_txt[3]);
                    player_map.put(player.getID(),player);
                }
            }
        }catch (Exception e) {
            throw new FileManageException("Error reading this txt data.file");
        }
        return this;
    }

    @Override
    public PlayerCRUD<String> update(HashMap<Player, DataOperation> changed_player_map) {
        return this;
    }

    /**
     * Writes the provided player data to a text file. Each {@code Player} entry from the given
     * {@code TreeMap} is written as a comma-separated line in the following format:
     * {@code ID,Region,Server,Name}. Each line corresponds to one player, derived from
     * {@code player_data.values()}.
     * <p>
     * If writing to the file fails, the method throws a {@code FileManageException}.
     *
     * @param player_map a {@code TreeMap<Integer, Player>} containing the player data to be written.
     *                    The map's values represent {@code Player} objects whose properties such as
     *                    {@code getID}, {@code getRegion}, {@code getServer}, and {@code getName}
     *                    are used to write the formatted data.
     * @throws FileManageException if an error occurs during the file writing process.
     */
    @Override
    public PlayerCRUD<String> export(TreeMap<Integer, Player> player_map) {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file,false))){
            for(Player player : player_map.values()){
                bw.write(player.getID() + ",");
                bw.write(player.getRegion() + ",");
                bw.write(player.getServer() + ",");
                bw.write(player.getName());
                bw.newLine();
            }
        }catch (Exception e){
            throw new FileManageException("Failed to write player data via TXT. Cause: " + e.getMessage());
        }
        return this;
    }

}
