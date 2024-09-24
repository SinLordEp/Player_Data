package utils;

import model.PlayerOperationData;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class XMLReader implements FileReader {
    private final Document document;


    public XMLReader(PlayerOperationData current_data) throws ParserConfigurationException, IOException, SAXException {
        File file = current_data.getFile();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.parse(file);
        document.getDocumentElement().normalize();
    }

    public HashMap<Integer, Player> parse_player() {
        if(document == null){
            JOptionPane.showMessageDialog(null,"Document is null, parse player failed");
            return null;
        }
        HashMap<Integer, Player> playerData = new HashMap<>();
        NodeList playerList = document.getElementsByTagName("player");
        if(playerList == null) return null;
        for (int i = 0; i < playerList.getLength(); i++) {
            Node playerNode = playerList.item(i);
            Player temp = new Player();
            if (playerNode.getNodeType() == Node.ELEMENT_NODE) {
                Element playerElement = (Element) playerNode;
                temp.setID(Integer.parseInt(playerElement.getAttribute("id")));
                temp.setRegion(getElementValue(playerElement, "region"));
                temp.setServer(getElementValue(playerElement, "server"));
                temp.setName(getElementValue(playerElement, "name"));
            }
            playerData.put(temp.getID(), temp);
        }
        return playerData;
    }

    public static HashMap<String,String[]> parse_region_server(File region_server) throws ParserConfigurationException, IOException, SAXException {
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        document = builder.parse(region_server);
        if(document == null){
            JOptionPane.showMessageDialog(null,"Document is null,parse region server failed");
            return null;
        }
        HashMap<String,String[]> regionServerMap = new HashMap<>();
        NodeList regionList = document.getElementsByTagName("region");
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

    private String getElementValue(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
}
