package data.file;

import Interface.FileDataWriter;
import exceptions.FileManageException;
import model.Player;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class PlayerFileWriter implements FileDataWriter<Map<?,?>> {
    @Override
    @SuppressWarnings("unchecked")
    public void write(String file_path, Map<?,?> input_data)  {
        File file = new File(file_path);
        TreeMap<Integer,Player> player_data = (TreeMap<Integer, Player>) input_data;
        String file_extension = file_path.substring(file_path.lastIndexOf("."));
        switch (file_extension){
            case ".dat": write_dat(file, player_data); break;
            case ".xml": write_xml(file, player_data); break;
            case ".txt": write_txt(file, player_data); break;
        }
    }

    private void write_dat(File player_file, TreeMap<Integer, Player> player_data)  {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(player_file,false))){
            if(player_data != null) {
                for (Player player : player_data.values()) {
                    oos.writeObject(player);
                }
            }
            oos.writeObject("EOF");
        }catch (IOException e){
            throw new FileManageException("Failed to write player data via DAT. Cause: " + e.getMessage());
        }
    }

    public void write_xml(File player_file, TreeMap<Integer, Player> player_data)  {
        try {
            Document document = xml_utils.createDocument();
            Element root = document.createElement("Player");
            document.appendChild(root);
            if(player_data != null){
                add_PlayerElements(document, root, player_data);
            }
            xml_utils.writeXml(document, player_file);
        } catch (Exception e) {
            throw new FileManageException("Failed to write player data via XML. Cause: " + e.getMessage());
        }
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

    private void write_txt(File player_file, TreeMap<Integer, Player> player_data)  {
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(player_file,false))){
            for(Player player : player_data.values()){
                bw.write(player.getID() + ",");
                bw.write(player.getRegion() + ",");
                bw.write(player.getServer() + ",");
                bw.write(player.getName());
                bw.newLine();
            }
        }catch (Exception e){
            throw new FileManageException("Failed to write player data via TXT. Cause: " + e.getMessage());
        }
    }
}
