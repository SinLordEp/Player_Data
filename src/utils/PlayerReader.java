package utils;

import model.GeneralOperationData;
import model.Player;
import model.PlayerOperationData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class PlayerReader implements DataReader {

    @Override
    public void read(GeneralOperationData current_data) throws Exception {
        switch (current_data.getFile_extension()){
            case "dat": read_dat((PlayerOperationData)current_data); break;
            case "xml": read_xml((PlayerOperationData) current_data); break;
            case "txt": read_txt((PlayerOperationData) current_data); break;
        }
    }

    private void read_dat(PlayerOperationData current_data) throws Exception {
        String status = "";
        if(current_data.getRegion_server() == null) {
            read_region_server(current_data);
            status += "Region server loaded";
        }
        HashMap<Integer, Player> player_data = new HashMap<>();

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(current_data.getFile()))) {
            //.readObject will always throw EOFException when reach the end of file
            while (true) {
                Player player = (Player) ois.readObject();
                if(player != null && current_data.isPlayer_Valid(player)){
                    player_data.put(player.getID(), player);
                }else{
                    throw new RuntimeException("Invalid data: player data is damaged");
                }
            }
        } catch (EOFException _) {
            if (player_data.isEmpty()) {
                status += "\nNo data found";
                JOptionPane.showMessageDialog(null, status);
            } else {
                current_data.setPlayer_data(player_data);
                status += "\nPlayer data imported";
                JOptionPane.showMessageDialog(null, status);
            }
            current_data.setFile_changed(false);
        }
    }

    private void read_xml(PlayerOperationData current_data) throws Exception {
        String status = "";
        // check region_server
        if (current_data.getRegion_server() == null) {
            read_region_server(current_data);
            status += "Region server loaded";
        }

        Element root = xml_utils.readXml(current_data.getFile());
        if (!"Player".equals(root.getNodeName())) {
            throw new RuntimeException("Invalid XML: Root element is not Player");
        }
        if (!root.hasChildNodes()) {
            status += "\nNo player data found";
            current_data.setFile_changed(false);
            JOptionPane.showMessageDialog(null, status);
            return;
        }
        // parsing
        HashMap<Integer, Player> player_data = new HashMap<>();
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

                // check valid
                if (current_data.isPlayer_Valid(player)) {
                    player_data.put(player.getID(), player);
                } else {
                    throw new RuntimeException("Invalid XML: player data is damaged");
                }
            }
        }
        current_data.setPlayer_data(player_data);
        current_data.setFile_changed(false);
        status += "\nPlayer data imported";
        JOptionPane.showMessageDialog(null, status);
    }

    private void read_txt(PlayerOperationData current_data) throws Exception {
        String status = "";
        if(current_data.getRegion_server() == null) {
            read_region_server(current_data);
            status += "Region server loaded";
        }
        HashMap<Integer, Player> player_data = new HashMap<>();
        try(Scanner scanner = new Scanner(current_data.getFile())){
            if(!scanner.hasNext()){
                status += "No data found";
            }else{
                while(scanner.hasNext()){
                    String[] player_txt = scanner.nextLine().split(",");
                    Player player = new Player();
                    player.setID(Integer.parseInt(player_txt[0]));
                    player.setRegion(player_txt[1]);
                    player.setServer(player_txt[2]);
                    player.setName(player_txt[3]);
                    if (current_data.isPlayer_Valid(player)) {
                        player_data.put(player.getID(), player);
                    } else {
                        throw new RuntimeException("Invalid TXT: player data is damaged");
                    }
                }
                status += "\nPlayer data imported";
            }
            current_data.setPlayer_data(player_data);
            current_data.setFile_changed(false);
            JOptionPane.showMessageDialog(null, status);
        }catch (Exception e) {
            JOptionPane.showMessageDialog(null, "El fichero no está en formato correcto y es ilegible");

        }
    }

    public static void read_region_server(PlayerOperationData current_data) throws Exception {
        Element root = xml_utils.readXml(new File("./src/main/servers.xml"));
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
        current_data.setRegion_server(regionServerMap);
    }

}
