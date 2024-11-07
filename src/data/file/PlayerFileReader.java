package data.file;

import GUI.GeneralMenu;
import Interface.FileDataReader;
import model.Player;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.TreeMap;

public class PlayerFileReader implements FileDataReader<Map<?,?>> {

    public PlayerFileReader() {

    }

    @Override
    public TreeMap<?,?> read(String file_path) throws Exception {
        File file = new File(file_path);
        String file_extension = file_path.substring(file_path.lastIndexOf("."));
        return switch (file_extension){
            case ".dat" -> read_dat(file);
            case ".xml" -> read_xml(file);
            case ".txt" -> read_txt(file);
            default -> throw new IllegalStateException("Unexpected value: " + file_extension);
        };
    }

    private TreeMap<Integer, Player> read_dat(File file) throws Exception {
        TreeMap<Integer, Player> player_data = new TreeMap<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            while (true) {
                Object temp = ois.readObject();
                if("EOF".equals(temp)){
                    break;
                }
                if(!(temp instanceof Player player)){
                    throw new Exception("Error reading Dat data.file, data.file is corrupted");
                }else{
                    player_data.put(player.getID(), player);
                }
            }
        }
        if (player_data.isEmpty()) {
            GeneralMenu.message_popup("No data found");
        }
        return player_data;
    }

    private TreeMap<Integer, Player> read_xml(File file) throws Exception {
        TreeMap<Integer, Player> player_data = new TreeMap<>();
        Element root = xml_utils.readXml(file);
        if (!"Player".equals(root.getNodeName())) {
            throw new RuntimeException("Invalid XML: Root element is not Player");
        }
        if (!root.hasChildNodes()) {
            GeneralMenu.message_popup("No player data found");
            return player_data;
        }
        // parsing
        NodeList playerNodes = root.getElementsByTagName("player");
        for (int i = 0; i < playerNodes.getLength(); i++) {
            Node playerNode = playerNodes.item(i);
            if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element playerElement = (Element) playerNode;
                Player player = new Player();
                player.setID(Integer.parseInt(playerElement.getAttribute("id")));
                player.setRegion(xml_utils.getElementTextContent(playerElement, "region"));
                player.setServer(xml_utils.getElementTextContent(playerElement, "server"));
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
                GeneralMenu.message_popup("No data found");
            }else{
                while(scanner.hasNext()){
                    String[] player_txt = scanner.nextLine().split(",");
                    Player player = new Player();
                    player.setID(Integer.parseInt(player_txt[0]));
                    player.setRegion(player_txt[1]);
                    player.setServer(player_txt[2]);
                    player.setName(player_txt[3]);
                    player_data.put(player.getID(),player);
                }
                System.out.println("Txt file reached the end");
            }
        }catch (Exception e) {
            throw new RuntimeException("Error reading this txt data.file");
        }
        return player_data;
    }

    public static HashMap<String, String[]> read_region_server() throws Exception {
        Element root = xml_utils.readXml(new File("./src/config/region_server.xml"));
        if (!"region_server".equals(root.getNodeName())) {
            throw new RuntimeException("Invalid XML: Root element is not region_server");
        }
        if (!root.hasChildNodes()) {
            throw new RuntimeException("Empty XML: No region_server data found");
        }
        // parsing
        HashMap<String, String[]> regionServerMap = new HashMap<>();
        NodeList regionList = root.getElementsByTagName("region");
        for (int i = 0; i < regionList.getLength(); i++) {
            Node regionNode = regionList.item(i);
            if (regionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element regionElement = (Element) regionNode;
                String regionName = regionElement.getAttribute("name");
                NodeList serverList = regionElement.getElementsByTagName("server");
                String[] servers = new String[serverList.getLength()];

                for (int j = 0; j < serverList.getLength(); j++) {
                    Element serverElement = (Element) serverList.item(j);
                    servers[j] = serverElement.getTextContent();
                }
                regionServerMap.put(regionName, servers);
            }
        }
        return regionServerMap;
    }

}
