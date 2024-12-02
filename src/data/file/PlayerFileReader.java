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
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class PlayerFileReader implements FileDataReader<Map<?,?>> {

    public PlayerFileReader() {
    }

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

    public static HashMap<Region, Server[]> read_region_server() {
        Element root = null;
        URL resource = PlayerFileReader.class.getResource(getProperty("regionServerConfig"));
        try {
            if (resource != null) {
                root = xml_utils.readXml(new File(resource.getFile()));
            }
            if(root == null){
                throw new FileManageException("Failed to read region server file");
            }
        } catch (Exception e) {
            throw new FileManageException("Failed to read region_server settings. Cause: " + e.getMessage());
        }
        if (!"region_server".equals(root.getNodeName())) {
            throw new RuntimeException("Invalid XML: Root element is not region_server");
        }
        if (!root.hasChildNodes()) {
            throw new RuntimeException("Empty XML: No region_server data found");
        }
        HashMap<Region, Server[]> regionServerMap = new HashMap<>();
        NodeList regionList = root.getElementsByTagName("region");
        for (int i = 0; i < regionList.getLength(); i++) {
            Node regionNode = regionList.item(i);
            if (regionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element regionElement = (Element) regionNode;
                Region region = new Region(regionElement.getAttribute("name"));
                NodeList serverList = regionElement.getElementsByTagName("server");
                Server[] servers = new Server[serverList.getLength()];

                for (int j = 0; j < serverList.getLength(); j++) {
                    Element serverElement = (Element) serverList.item(j);
                    servers[j] = new Server(serverElement.getTextContent(), region);
                }
                regionServerMap.put(region, servers);
            }
        }
        return regionServerMap;
    }

}
