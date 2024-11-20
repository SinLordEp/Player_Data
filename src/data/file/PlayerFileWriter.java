package data.file;

import GUI.GeneralDialog;
import Interface.FileDataWriter;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class PlayerFileWriter implements FileDataWriter<Map<?,?>> {
    @Override
    @SuppressWarnings("unchecked")
    public void write(String file_path, Map<?,?> input_data) throws Exception {
        File file = new File(file_path);
        TreeMap<Integer,Player> player_data = (TreeMap<Integer, Player>) input_data;


        String file_extension = file_path.substring(file_path.lastIndexOf("."));
        switch (file_extension){
            case ".dat": write_dat(file, player_data); break;
            case ".xml": write_xml(file, player_data); break;
            case ".txt": write_txt(file, player_data); break;
        }
        GeneralDialog.getDialog().popup("file_saved");
    }

    private void write_dat(File player_file, TreeMap<Integer, Player> player_data) throws Exception {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(player_file,false))){
            if(player_data != null) {
                for (Player player : player_data.values()) {
                    oos.writeObject(player);
                }
            }
            oos.writeObject("EOF");
        }
    }

    public void write_xml(File player_file, TreeMap<Integer, Player> player_data) throws Exception {
        Document document = xml_utils.createDocument();
        // create Player root
        Element root = document.createElement("Player");
        // Add root to data.file
        document.appendChild(root);
        // read data and transform to xml element then add to root
        if(player_data != null){
            add_PlayerElements(document, root, player_data);
        }
        // save to data.file
        xml_utils.writeXml(document, player_file);
    }

    private void add_PlayerElements(Document document, Element root, TreeMap<Integer, Player> player_data) {
        for (Player player : player_data.values()) {
            Element playerElement = document.createElement("player");
            playerElement.setAttribute("id", String.valueOf(player.getID()));
            xml_utils.createElementWithText(document, playerElement, "region", player.getRegion());
            xml_utils.createElementWithText(document, playerElement, "server", player.getServer());
            xml_utils.createElementWithText(document, playerElement, "name", player.getName());
            root.appendChild(playerElement);
        }
    }

    private void write_txt(File player_file, TreeMap<Integer, Player> player_data){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(player_file,false))){
            for(Player player : player_data.values()){
                bw.write(player.getID() + ",");
                bw.write(player.getRegion() + ",");
                bw.write(player.getServer() + ",");
                bw.write(player.getName());
                bw.newLine();
            }
        }catch (Exception e){
            GeneralDialog.getDialog().popup("data_invalid");
        }
    }
}
