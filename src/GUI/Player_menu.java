package GUI;

import model.PlayerOperationData;
import model.Player;
import utils.*;

import javax.swing.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static GUI.GUI_utils.buildSelectionDialog;


public class Player_menu {

    public static void main_menu(PlayerOperationData current_data){
        String[] person_class = {"Player", "GM (Currently Not Available)"};
        while(true){
            try{
                current_data.setPerson_type(switch (buildSelectionDialog("Choose a person type", person_class)){
                    case 1 -> person_class[0];
                    case 2 -> throw new Exception("Currently Not Available");
                    case -1 -> throw new Exception("Operation canceled");
                    default -> throw new Exception("Option invalid");
                });
                fileType_menu(current_data);
            }catch (Exception e){
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    static void fileType_menu(PlayerOperationData current_data){
        String[] options = {"Dat File", "XML File"};
        while(current_data.getFile() == null){
            try{
                //Set extension
                current_data.setFile_extension(switch (buildSelectionDialog("Choose a data source", options)) {
                    case 1 -> "dat";
                    case 2 -> "xml";
                    case -1 -> throw new Exception("Operation canceled");
                    default -> "";
                });
                // Choose file
                JOptionPane.showMessageDialog(null,"Choosing source file");
                current_data.setFile(File_Chooser.get_path(current_data.getFile_extension()));
                if(current_data.getFile() == null) throw new Exception("Operation canceled");
                //Read file data and save to PlayerOperationData
                FileReader fileReader = FileReaderFactory.getFileReader(current_data);
                current_data.setPlayer_data(fileReader.parse_player());
                if(current_data.getPlayer_data() == null) throw new Exception("Operation canceled");
                operation_menu(current_data);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }

    }

    static void operation_menu(PlayerOperationData current_data){
        String[] options = {"Show Data", "Write File", "Convert to .dat File"};
        while (true){
            try{
                switch(buildSelectionDialog("""
                        Path: %s
                        Choose a operation""".formatted(current_data.getAbsolutePath()), options)){
                    case 1: current_data.print_person(); break;
                    case 2: update(current_data);
                        if(current_data.isFile_changed()) update_file(current_data);
                        break;
                    case 3: convert_toDAT(); break;
                    case -1: throw new Exception("Operation canceled");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    static void update(PlayerOperationData current_data) throws Exception {
        String[] options = {"Create new Player", "Modify Player data"};
        switch(buildSelectionDialog("Choose a operation", options)){
            case 1:
                Player player = create_player(current_data);
                if(!player.isValid()) return;
                current_data.getPlayer_data().put(player.getID(), player);
                current_data.setFile_changed(true);
                break;
            case 2:
                modify_player(current_data);
                break;
            case -1: throw new Exception("Operation canceled");
        }
    }

    public static void modify_player(PlayerOperationData current_data) throws Exception {
        String[] options = {"Region", "Server", "Name", "ALL"};
        int ID = Integer.parseInt(JOptionPane.showInputDialog("Enter the ID to modify"));
        if(!current_data.containsKey(ID)){
            JOptionPane.showMessageDialog(null,"Player does not exist");
            return;
        }
        Player player = current_data.getFrom_Map(ID);
        switch(buildSelectionDialog("Choose the data you need to modify", options)){
            // case 1 is linked to case 2, because after changing region the server has to be changed too.
            case 1: player.setRegion(region_chooser(current_data));
            case 2: player.setServer(server_chooser(current_data, player.getRegion())); break;
            case 3: player.setName(JOptionPane.showInputDialog("Enter player name: ")); break;
            case 4: player.setRegion(region_chooser(current_data));
                player.setServer(server_chooser(current_data, player.getRegion()));
                player.setName(JOptionPane.showInputDialog("Enter player name: "));
                break;
            case -1: throw new Exception("Operation canceled");
        }
        if(player.isValid()){
            current_data.putIn_Map(ID, player);
        }else{
            throw new Exception("Player data is invalid");
        }
    }

    public static void convert_toDAT(){

    }

    public static void update_file(PlayerOperationData current_data) throws Exception {
        switch (current_data.getFile_extension()){
            case "dat": new DATWriter(current_data).write_player(); break;
            case "xml": new XMLWriter(current_data).write_player(); break;
        }
    }

    public static Player create_player(PlayerOperationData current_data) throws Exception {
        if(current_data.getRegion_server() == null){
            current_data.setRegion_server(XMLReader.parse_region_server(new File("./src/main/servers.xml")));
        }
        Player player = new Player();
        player.setRegion(region_chooser(current_data));
        player.setServer(server_chooser(current_data, player.getRegion()));
        player.setID(create_ID(current_data));
        player.setName(JOptionPane.showInputDialog("Enter person Name: "));
        return player;
    }

    public static int create_ID(PlayerOperationData current_data)  {
        while (true) {
            try {
                int ID = Integer.parseInt(JOptionPane.showInputDialog("Input ID number: "));
                if (current_data.containsKey(ID)) {
                    JOptionPane.showMessageDialog(null, "ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null,"ID format incorrect");
            }
        }
    }

    public  static String region_chooser(PlayerOperationData current_data) throws Exception {
        Set<String> regions = current_data.getRegion_server().keySet();
        List<String> regionList = new ArrayList<>(regions);
        int option = buildSelectionDialog("Choose a region: " , regionList.toArray(new String[0]));
        if(option == -1) throw new Exception("Operation canceled");
        return regionList.get(option-1);
    }

    public static String server_chooser(PlayerOperationData current_data, String region) throws Exception {
        String[] servers = current_data.getServer(region);
        int option = buildSelectionDialog("Choose a server: ", servers);
        if(option == -1) throw new Exception("Operation canceled");
        return servers[option-1];
    }
}
