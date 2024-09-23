package utils;

import model.Person;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class XML_Reader implements File_Manager {

    public XML_Reader() {
    }

    public Document file_reading(File file){
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String,String[]> parse_region_server(Document document){
        if(document == null){
            JOptionPane.showMessageDialog(null,"Document is null,parse region server failed");
            return null;
        }
        Map<String,String[]> regionServerMap = new HashMap<>();
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

    public Map<Integer, Person> parse_player(Document document){
        if(document == null){
            JOptionPane.showMessageDialog(null,"Document is null, parse player failed");
            return null;
        }
        Map<Integer, Person> playerData = new HashMap<>();
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

    private String getElementValue(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

}
