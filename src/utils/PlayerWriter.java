package utils;

import model.GeneralOperationData;
import model.Player;
import model.PlayerOperationData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class PlayerWriter implements DataWriter {
    @Override
    public void write(GeneralOperationData current_data) throws Exception {
        switch (current_data.getFile_extension()){
            case "dat": write_dat((PlayerOperationData) current_data); break;
            case "xml": write_xml((PlayerOperationData) current_data); break;
        }
    }

    private void write_dat(PlayerOperationData current_data) throws Exception {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(current_data.getFile(),false))){
            for(Player player : current_data.getPlayer_data().values()){
                oos.writeObject(player);
            }
        }
    }

    public void write_xml(PlayerOperationData current_data) throws Exception {
        Document document = xml_utils.createDocument();

        // create Player root
        Element root = document.createElement("Player");
        // Add root to file
        document.appendChild(root);
        // read data and transform to xml element then add to root
        add_PlayerElements(document, root, current_data.getPlayer_data());
        // save to file
        xml_utils.writeXml(document, current_data.getFile());
        JOptionPane.showMessageDialog(null, "New Player added");
    }

    private void add_PlayerElements(Document document, Element root, HashMap<Integer, Player> player_data) {
        for (Player player : player_data.values()) {
            Element playerElement = document.createElement("player");
            playerElement.setAttribute("id", String.valueOf(player.getID()));
            xml_utils.createElementWithText(document, playerElement, "region", player.getRegion());
            xml_utils.createElementWithText(document, playerElement, "server", player.getServer());
            xml_utils.createElementWithText(document, playerElement, "name", player.getName());
            root.appendChild(playerElement);
        }
    }
}
