package utils;

import model.PersonOperationData;
import model.Person;
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
import java.util.Map;

public class XMLReader implements FileReader<Object> {
    private final Document document;
    private final String data_type;
    public XMLReader(PersonOperationData current_data) throws ParserConfigurationException, IOException, SAXException {
        File file = current_data.getFile();
        this.data_type = current_data.getPerson_type();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.parse(file);
        document.getDocumentElement().normalize();
    }

    @Override
    public Object read() throws Exception {
        return switch (data_type){
            case "Region_server" -> parse_region_server();
            case "Player" -> parse_player();
            case "GM" -> throw new Exception("Type is not available yet");
            default -> throw new IllegalStateException("Unexpected data type");
        };
    }


    @Override
    public HashMap<Integer, Person> parse_player() {
        if(document == null){
            JOptionPane.showMessageDialog(null,"Document is null, parse player failed");
            return null;
        }
        HashMap<Integer, Person> playerData = new HashMap<>();
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

    public Map<String,String[]> parse_region_server(){
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

    private String getElementValue(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
}
