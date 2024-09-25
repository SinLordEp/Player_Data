package main;

import GUI.Player_menu;
import model.Player;
import model.PlayerOperationData;
import utils.DataReaderFactory;
import utils.DataWriterFactory;
import utils.FileManager;


import javax.swing.*;
import java.util.HashMap;


public class Player_control {
    public static void player_main(PlayerOperationData current_data){
        while(current_data.getFile() == null){
            try{
                current_data.setFile_extension(extension_menu());
                //Initialize reader,writer,file manager
                DataReaderFactory.initialReader(current_data);
                DataWriterFactory.initializeWriter(current_data);
                FileManager fileManager = new FileManager();
                file_operation(current_data, fileManager);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    private static String extension_menu() throws Exception {
        return switch (Player_menu.extension_player()) {
            case 1 -> "dat";
            case 2 -> "xml";
            case 3 -> "txt";
            case -1 -> throw new Exception("Operation canceled");
            default -> "";
        };
    }
    private static void file_operation(PlayerOperationData current_data, FileManager fileManager){
        while (current_data.getFile() == null){
            try{
                switch (Player_menu.file_menu()){
                    case 1:
                        current_data.setFile(FileManager.create_file(current_data.getFile_extension()));
                        fileManager.writeData(current_data);
                        current_data.setFile_changed(true);
                        break;
                    case 2:
                        current_data.setFile(fileManager.read_file(current_data.getFile_extension()));
                        current_data.setFile_changed(true);
                        break;
                    case -1: throw new Exception("Operation canceled");
                }
                if(current_data.isFile_changed()){
                    fileManager.readData(current_data);
                    current_data.setFile_changed(false);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
        operation(current_data, fileManager);
    }

    private static void operation(PlayerOperationData current_data, FileManager fileManager){
        while (true){
            try{
                switch(Player_menu.operation_menu(current_data.getAbsolutePath())){
                    case 1: current_data.print_person(); break;
                    case 2: modify_operation(current_data, fileManager); break;
                    case 3: export_menu(current_data, fileManager); break;
                    case -1: throw new Exception("Operation canceled");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    private static void modify_operation(PlayerOperationData current_data, FileManager fileManager){
        while(true){
            try {
                switch(Player_menu.modify_menu()){
                    case 1: create_player(current_data); break;
                    case 2: modify_player_operation(current_data); break;
                    case 3: delete_player(current_data); break;
                    case -1: throw new Exception("Operation canceled");
                }
                if(current_data.isFile_changed()) {
                    fileManager.writeData(current_data);
                    current_data.setFile_changed(false);
                    return;
                }
            }catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
                if(e.getMessage().equals("Operation canceled")) return;
            }
        }
    }

    private static void modify_player_operation(PlayerOperationData current_data) throws Exception {
        if(current_data.getPlayer_data() == null){
            Player_menu.message("Empty Map");
            return;
        }
        int ID = Player_menu.ID_input_UI();
        if(!current_data.containsKey(ID)) throw new Exception("Player does not exist");
        Player player = current_data.getFrom_Map(ID);
        switch(Player_menu.modify_player_menu()){
            // case 1 is linked to case 2, because after changing region the server has to be changed too.
            case 1: player.setRegion(Player_menu.region_chooser(current_data));
            case 2: player.setServer(Player_menu.server_chooser(current_data, player.getRegion())); break;
            case 3: player.setName(Player_menu.name_input_UI()); break;
            case 4: player.setRegion(Player_menu.region_chooser(current_data));
                player.setServer(Player_menu.server_chooser(current_data, player.getRegion()));
                player.setName(Player_menu.name_input_UI());
                break;
            case -1: throw new Exception("Operation canceled");
        }
            current_data.putIn_Map(ID, player);
            current_data.setFile_changed(true);
            Player_menu.message("Modify");
    }

    private static void create_player(PlayerOperationData current_data) throws Exception {
        Player player = new Player();
        player.setRegion(Player_menu.region_chooser(current_data));
        player.setServer(Player_menu.server_chooser(current_data, player.getRegion()));
        player.setID(create_ID(current_data));
        player.setName(Player_menu.name_input_UI());
        if(current_data.isPlayer_Valid(player)) {
            if(current_data.getPlayer_data() == null){
                current_data.setPlayer_data(new HashMap<>());
            }
            current_data.getPlayer_data().put(player.getID(), player);
            current_data.setFile_changed(true);
        }else{
            throw new Exception("Player data is invalid");
        }
    }

    private static int create_ID(PlayerOperationData current_data) throws Exception {
        while (true) {
            try {
                int ID = Player_menu.ID_input_UI();
                if (current_data.containsKey(ID)) {
                    throw new Exception("ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                Player_menu.message("ID Format");
            }
        }
    }

    private static void delete_player(PlayerOperationData current_data) throws Exception {
        if(current_data.getPlayer_data() == null){
            Player_menu.message("Empty Map");
            return;
        }
        int ID = Player_menu.ID_input_UI();
        if(!current_data.containsKey(ID)) throw new Exception("Player does not exist");
        current_data.getPlayer_data().remove(ID);
        current_data.setFile_changed(true);
    }

    private static void export_menu(PlayerOperationData current_data, FileManager fileManager) throws Exception {
        if(current_data.getPlayer_data() == null){
            Player_menu.message("Empty Map");
            return;
        }
        String target_extension = extension_menu();
        fileManager.exportData(target_extension,current_data);
    }

    private static void exception_handler(Exception e){

    }
}
