package data.file;

import Interface.FileDataWriter;
import exceptions.FileManageException;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * The {@code PlayerFileWriter} class provides functionality to write player data, represented as a map,
 * into a file. The file format for output is determined by the file extension provided in the target file path.
 * It implements the {@code FileDataWriter<Map<?, ?>>} interface.
 * <p>
 * The supported file formats include:
 * - DAT: Serialized binary format.
 * - XML: Structured hierarchical textual format.
 * - TXT: Plain text format with comma-separated values.
 * <p>
 * Helper methods handle the specific logic for writing to different file formats:
 * {@code write_dat}, {@code write_xml}, and {@code write_txt}.
 * @author SIN
 */
public class PlayerFileWriter implements FileDataWriter<Map<?,?>> {
    /**
     * Writes the given player data into a file at the specified file path. The file format is determined by
     * the file extension provided in the {@code file_path}. Supported formats include `.dat`, `.xml`, and `.txt`.
     * This method delegates the actual file writing operation to appropriate helper methods:
     * {@code write_dat}, {@code write_xml}, or {@code write_txt}.
     *
     * @param file_path the path where the file will be written, including the file name and extension.
     *                  The extension determines the file format.
     * @param input_data a {@code Map} containing the data to be written. The data is expected to be a
     *                   {@code TreeMap<Integer, Player>} within the provided {@code Map}.
     *                   Other map types or null values may cause runtime exceptions.
     * @throws FileManageException if an error occurs during file writing in any of the delegated methods.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void write(String file_path, Map<?,?> input_data)  {
        File file = new File(file_path);
        TreeMap<Integer,Player> player_data = (TreeMap<Integer, Player>) input_data;
        String file_extension = file_path.substring(file_path.lastIndexOf("."));
        switch (file_extension){
            case ".dat": write_dat(file, player_data); break;
            case ".xml": write_xml(file, player_data); break;
            case ".txt": write_txt(file, player_data); break;
        }
    }

    /**
     * Writes the provided player data to the specified file in DAT format. Each {@code Player} object
     * in the {@code player_data} map is serialized using an {@code ObjectOutputStream}. The method
     * appends an "EOF" marker at the end of the file to signify the end of content.
     *
     * @param player_file the file where the player data will be written. The file is overwritten if it already exists.
     * @param player_data a {@code TreeMap<Integer, Player>} containing the player data to be serialized and written to the file.
     *                    If the map is null or empty, only the "EOF" marker will be written.
     * @throws FileManageException if an error occurs during file writing, such as an {@code IOException}.
     */
    private void write_dat(File player_file, TreeMap<Integer, Player> player_data)  {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(player_file,false))){
            if(player_data != null) {
                for (Player player : player_data.values()) {
                    oos.writeObject(player);
                }
            }
            oos.writeObject("EOF");
        }catch (IOException e){
            throw new FileManageException("Failed to write player data via DAT. Cause: " + e.getMessage());
        }
    }

    /**
     * Writes the provided player data into an XML file at the specified file location.
     * The method uses {@code xml_utils.createDocument} to generate a new XML document,
     * {@code add_PlayerElements} to populate the XML with player data, and
     * {@code xml_utils.writeXml} to write the generated XML structure to the provided file.
     *
     * @param player_file the file where the player data will be written. If the file already exists,
     *                    it will be overwritten.
     * @param player_data a {@code TreeMap<Integer, Player>} containing player data to write. If the map
     *                    is null, the resulting XML file will contain only the root element without any
     *                    player data.
     * @throws FileManageException if an error occurs during XML creation, data population, or file writing.
     */
    public void write_xml(File player_file, TreeMap<Integer, Player> player_data)  {
        try {
            Document document = xml_utils.createDocument();
            Element root = document.createElement("Player");
            document.appendChild(root);
            if(player_data != null){
                add_PlayerElements(document, root, player_data);
            }
            xml_utils.writeXml(document, player_file);
        } catch (Exception e) {
            throw new FileManageException("Failed to write player data via XML. Cause: " + e.getMessage());
        }
    }

    /**
     * Populates the given XML document with player data by creating and appending player elements
     * as child nodes of the specified root element. Each player element includes attributes
     * and child nodes representing player details such as ID, region, server, and name.
     * <p>
     * The method utilizes {@code xml_utils.createElementWithText} to help create child nodes
     * with text content for each player's properties.
     *
     * @param document the XML document where player data will be added. This document is expected
     *                 to be initialized prior to invoking the method.
     * @param root the root XML element to which the player elements will be appended.
     * @param player_data a {@code TreeMap<Integer, Player>} containing player information. Each
     *                    entry's key represents a unique player ID, and the value is a {@code Player}
     *                    object which contains the details to be inserted into the XML.
     */
    private void add_PlayerElements(Document document, Element root, TreeMap<Integer, Player> player_data) {
        for (Player player : player_data.values()) {
            Element playerElement = document.createElement("player");
            playerElement.setAttribute("id", String.valueOf(player.getID()));
            xml_utils.createElementWithText(document, playerElement, "region", player.getRegion().toString());
            xml_utils.createElementWithText(document, playerElement, "server", player.getServer().toString());
            xml_utils.createElementWithText(document, playerElement, "name", player.getName());
            root.appendChild(playerElement);
        }
    }

    /**
     * Writes the provided player data to a text file. Each {@code Player} entry from the given
     * {@code TreeMap} is written as a comma-separated line in the following format:
     * {@code ID,Region,Server,Name}. Each line corresponds to one player, derived from
     * {@code player_data.values()}.
     * <p>
     * If writing to the file fails, the method throws a {@code FileManageException}.
     *
     * @param player_file the file where the player data will be written. The existing content of
     *                    the file will be overwritten.
     * @param player_data a {@code TreeMap<Integer, Player>} containing the player data to be written.
     *                    The map's values represent {@code Player} objects whose properties such as
     *                    {@code getID}, {@code getRegion}, {@code getServer}, and {@code getName}
     *                    are used to write the formatted data.
     * @throws FileManageException if an error occurs during the file writing process.
     */
    private void write_txt(File player_file, TreeMap<Integer, Player> player_data)  {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(player_file,false))){
            for(Player player : player_data.values()){
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
