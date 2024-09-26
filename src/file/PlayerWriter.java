package file;

import GUI.GeneralMenu;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerWriter implements DataWriter<Map<?,?>> {
    @Override
    @SuppressWarnings("unchecked")
    public void write(String file_path, Map<?,?> input_data) throws Exception {
        File file = new File(file_path);
        HashMap<Integer,Player> player_data = (HashMap<Integer, Player>) input_data;
        if(file.createNewFile()){
            GeneralMenu.message_popup("New file created");
        }

        String file_extension = file_path.substring(file_path.lastIndexOf("."));
        switch (file_extension){
            case ".dat": write_dat(file, player_data); break;
            case ".xml": write_xml(file, player_data); break;
            case ".txt": write_txt(file, player_data); break;
        }
        GeneralMenu.message_popup("File saved correctly");
    }

    private void write_dat(File player_file, HashMap<Integer, Player> player_data) throws Exception {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(player_file,false))){
            if(player_data != null) {
                for (Player player : player_data.values()) {
                    oos.writeObject(player);
                }
            }
            oos.writeObject("EOF");
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
