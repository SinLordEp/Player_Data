package utils;

import model.GeneralOperationData;
import model.Player;
import model.PlayerOperationData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;

public class PlayerWriter implements DataWriter {
    @Override
    public void write(GeneralOperationData current_data) throws Exception {
        File file = current_data.getFile();
        HashMap<Integer, Player> player_data = ((PlayerOperationData) current_data).getPlayer_data();
        switch (current_data.getFile_extension()){
            case "dat": write_dat(file, player_data); break;
            case "xml": write_xml(file, player_data); break;
        }
        JOptionPane.showMessageDialog(null, "File updated");
    }

    @Override
    public void export(String file_extension, GeneralOperationData current_data) throws Exception {
        File exporting_file = FileManager.create_file(file_extension);
        HashMap<Integer, Player> player_data = ((PlayerOperationData) current_data).getPlayer_data();
        switch(file_extension){
            case "dat": write_dat(exporting_file, player_data); break;
            case "xml": write_xml(exporting_file, player_data); break;
            case "txt": write_txt(exporting_file, player_data); break;
        }
        JOptionPane.showMessageDialog(null, "File exporting process finished");
    }

    private void write_dat(File player_file, HashMap<Integer, Player> player_data) throws Exception {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(player_file,false))){
            if(player_data != null) {
                for (Player player : player_data.values()) {
                    oos.writeObject(player);
                }
            }
        }
    }

    public void write_xml(File player_file, HashMap<Integer, Player> player_data) throws Exception {
        Document document = xml_utils.createDocument();

        // create Player root
        Element root = document.createElement("Player");
        // Add root to file
        document.appendChild(root);
        // read data and transform to xml element then add to root
        if(player_data != null) add_PlayerElements(document, root, player_data);
        // save to file
        xml_utils.writeXml(document, player_file);
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

    private void write_txt(File player_file, HashMap<Integer, Player> player_data){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(player_file,false))){
            for(Player player : player_data.values()){
                bw.write(player.getID() + ",");
                bw.write(player.getRegion() + ",");
                bw.write(player.getServer() + ",");
                bw.write(player.getName() + ",");
                bw.newLine();
            }
        }catch (Exception e){
            JOptionPane.showMessageDialog(null,"Datos inválidos");
        }
    }
}
