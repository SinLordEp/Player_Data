package data.file;

import GUI.Player.PlayerText;
import Interface.PlayerFDA;
import exceptions.FileManageException;
import model.Player;
import model.Region;
import model.Server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class TextFDA implements PlayerFDA {

    /**
     * Reads and parses a TXT file containing player data, converting it into a {@code TreeMap}
     * where the key is the player's ID and the value is the corresponding {@code Player} object.
     * Each line in the file is expected to contain player details in comma-separated format:
     * {@code PlayerID, RegionName, ServerName, PlayerName}.
     * <p>
     * If the file is empty, it displays a popup notification using {@code PlayerText.getDialog().popup}.
     * In case of any error, it throws a {@code FileManageException}.
     *
     * @param file The {@code File} object representing the TXT file to be read.
     * @return A {@code TreeMap<Integer, Player>} where the key is the player ID and the value is the corresponding {@code Player} object.
     *         Returns an empty map if the file contains no player data.
     * @throws FileManageException if there is an error during file reading.
     */
    @Override
    public TreeMap<Integer, Player> read(File file) {
        TreeMap<Integer, Player> player_data = new TreeMap<>();
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
                    player_data.put(player.getID(),player);
                }
            }
        }catch (Exception e) {
            throw new FileManageException("Error reading this txt data.file");
        }
        return player_data;
    }

    /**
     * Writes the provided player data to a text file. Each {@code Player} entry from the given
     * {@code TreeMap} is written as a comma-separated line in the following format:
     * {@code ID,Region,Server,Name}. Each line corresponds to one player, derived from
     * {@code player_data.values()}.
     * <p>
     * If writing to the file fails, the method throws a {@code FileManageException}.
     *
     * @param file the file where the player data will be written. The existing content of
     *                    the file will be overwritten.
     * @param player_map a {@code TreeMap<Integer, Player>} containing the player data to be written.
     *                    The map's values represent {@code Player} objects whose properties such as
     *                    {@code getID}, {@code getRegion}, {@code getServer}, and {@code getName}
     *                    are used to write the formatted data.
     * @throws FileManageException if an error occurs during the file writing process.
     */
    @Override
    public void write(File file, TreeMap<Integer, Player> player_map) {
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
    }

}
