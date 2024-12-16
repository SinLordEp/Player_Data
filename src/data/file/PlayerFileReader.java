package data.file;

import GUI.Player.PlayerText;
import Interface.FileDataReader;
import exceptions.FileManageException;
import model.Player;
import model.Region;
import model.Server;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.openmbean.OpenDataException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class PlayerFileReader implements FileDataReader<Map<?,?>> {

    /**
     * Constructs a new {@code PlayerFileReader} instance.
     * This class is responsible for reading player data from files of various types
     * such as TXT, DAT, and XML. It uses delegated private methods including
     * {@code read_txt}, {@code read_dat}, and {@code read_xml} for parsing specific file formats.
     * <p>
     * The {@code PlayerFileReader} interacts with different utilities and methods
     * to ensure that data is properly read and converted into a usable format
     * (e.g., {@code TreeMap}). This class primarily supports {@code FileType} enums
     * like {@code FileType.TXT}, {@code FileType.DAT}, and {@code FileType.XML}.
     * <p>
     * The class is typically utilized in conjunction with other data management
     * components such as {@code PlayerFileWriter}, database access classes, and PHP export handlers,
     * ensuring consistent and accurate operations for player data management workflows.
     */
    public PlayerFileReader() {
    }

    /**
     * Reads data from a file of the specified type and converts it into a {@code TreeMap}.
     * This method delegates handling of specific file types to the corresponding private methods:
     * {@code read_txt}, {@code read_dat}, and {@code read_xml}.
     *
     * @param fileType The type of file to be read. Supported types are {@code FileType.TXT},
     *                 {@code FileType.DAT}, {@code FileType.XML}, and {@code FileType.NONE}.
     * @param file_path The path to the file to be read.
     * @return A {@code TreeMap} containing the parsed data. Returns {@code null} if the
     *         {@code fileType} is {@code FileType.NONE}, or an empty map if the file is empty.
     * @throws FileManageException if there is an error during file reading.
     */
    @Override
    public TreeMap<?,?> read(FileType fileType, String file_path) {
        File file = new File(file_path);
        return switch (fileType){
            case NONE -> null;
            case TXT -> read_txt(file);
            case DAT -> read_dat(file);
            case XML -> read_xml(file);
        };
    }

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
    private TreeMap<Integer, Player> read_dat(File file) {
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
     * Reads and parses an XML file containing player data, storing the information
     * in a {@code TreeMap} where the key is the player's ID and the value is the
     * corresponding {@code Player} object. The XML file must have a root element
     * named "Player" and child elements named "player" with required attributes and
     * child elements.
     * <p>
     * If the XML file does not meet the expected structure or an error occurs
     * during the reading process, exceptions are thrown or error handling
     * mechanisms are invoked.
     * <p>
     * This method utilizes {@code xml_utils.readXml} to parse the XML document and
     * {@code xml_utils.getElementTextContent} to retrieve element contents.
     * <p>
     * Displays a popup notification using {@code PlayerText.getDialog().popup}
     * when the XML document has no child nodes. Throws a {@code FileManageException}
     * if the root element is invalid or if there is an error during file handling.
     *
     * @param file The {@code File} object representing the XML file to be read.
     * @return A {@code TreeMap<Integer, Player>} where the key is the player ID and
     *         the value is the corresponding {@code Player} object. Returns an empty
     *         map if the file is empty or contains no player data.
     * @throws FileManageException if there is an error during XML reading or if the
     *                             root element is invalid.
     */
    private TreeMap<Integer, Player> read_xml(File file) {
        TreeMap<Integer, Player> player_data = new TreeMap<>();
        Element root;
        try {
            root = xml_utils.readXml(file);
        } catch (Exception e) {
            throw new FileManageException("Failed to read xml file. Cause: " + e.getMessage());
        }
        if (!"Player".equals(root.getNodeName())) {
            throw new FileManageException("Invalid XML: Root element is not Player");
        }
        if (!root.hasChildNodes()) {
            PlayerText.getDialog().popup("player_map_null");
            return player_data;
        }
        NodeList playerNodes = root.getElementsByTagName("player");
        for (int i = 0; i < playerNodes.getLength(); i++) {
            Node playerNode = playerNodes.item(i);
            if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element playerElement = (Element) playerNode;
                Player player = new Player();
                player.setID(Integer.parseInt(playerElement.getAttribute("id")));
                player.setRegion(new Region(xml_utils.getElementTextContent(playerElement, "region")));
                player.setServer(new Server(xml_utils.getElementTextContent(playerElement, "server"), player.getRegion()));
                player.setName(xml_utils.getElementTextContent(playerElement, "name"));
                player_data.put(player.getID(), player);
            }
        }
        return player_data;
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
     * @param file The {@code File} object representing the TXT file to be read.
     * @return A {@code TreeMap<Integer, Player>} where the key is the player ID and the value is the corresponding {@code Player} object.
     *         Returns an empty map if the file contains no player data.
     * @throws FileManageException if there is an error during file reading.
     */
    private TreeMap<Integer, Player> read_txt(File file) {
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

}
