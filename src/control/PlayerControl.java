package control;

import GUI.PlayerMenu;
import model.Player;
import data.PlayerData;
import utils.DataReaderFactory;
import utils.DataWriterFactory;
import utils.FileManager;
import utils.OperationCanceledException;


import java.util.HashMap;

public class PlayerControl implements GeneralControl {
    private PlayerData playerData;
    @Override
    public void run() {
        this.playerData = new PlayerData();
        while(playerData.getFile() == null){
            try{
                playerData.setFile_extension(extension_control());
                //Initialize reader,writer,file manager
                DataReaderFactory.initialReader(playerData);
                DataWriterFactory.initializeWriter(playerData);
                FileManager fileManager = new FileManager();
                file_operation(fileManager);
            }catch (OperationCanceledException e) {
                PlayerMenu.exception_message(e);
                return;
            }catch (Exception e) {
                PlayerMenu.exception_message(e);
            }
        }
    }

    private static String extension_control() {
        return switch (PlayerMenu.extension_player()) {
            case "Binary DAT File" -> "dat";
            case "XML File" -> "xml";
            case "TXT File" -> "txt";
            default -> throw new IllegalArgumentException("Unknown extension");
        };
    }

    private void file_operation(FileManager fileManager){
        while (playerData.getFile() == null){
            try{
                switch (PlayerMenu.file_menu()){
                    case "Create new storage file":
                        playerData.setFile(FileManager.create_file(playerData.getFile_extension()));
                        fileManager.writeData(playerData);
                        playerData.setFile_changed(true);
                        break;
                    case "Read from existed file":
                        playerData.setFile(fileManager.read_file(playerData.getFile_extension()));
                        playerData.setFile_changed(true);
                        break;
                }
                if(playerData.isFile_changed()){
                    fileManager.readData(playerData);
                    playerData.setFile_changed(false);
                }
            }catch (OperationCanceledException e) {
                PlayerMenu.exception_message(e);
                return;
            } catch (Exception e) {
                PlayerMenu.exception_message(e);
            }
        }
        operation_control(fileManager);
    }

    private void operation_control(FileManager fileManager){
        while (true){
            try{
                switch(PlayerMenu.operation_menu(playerData.getAbsolutePath())){
                    case "Show Data": playerData.print_person(); break;
                    case "Modify data": modify_operation(fileManager); break;
                    case "Export data": export_menu(fileManager); break;
                }
            }catch (OperationCanceledException e) {
                PlayerMenu.exception_message(e);
                return;
            } catch (Exception e) {
                PlayerMenu.exception_message(e);
            }
        }
    }

    private void modify_operation(FileManager fileManager){
        while(true){
            try {
                switch(PlayerMenu.modify_menu()){
                    case "Create new Player": create_player(); break;
                    case "Modify Player data": modify_player_operation(); break;
                    case "Delete Player": delete_player(); break;
                }
                if(playerData.isFile_changed()) {
                    fileManager.writeData(playerData);
                    playerData.setFile_changed(false);
                    return;
                }
            }catch (OperationCanceledException e) {
                PlayerMenu.exception_message(e);
                return;
            }catch (Exception e) {
                PlayerMenu.exception_message(e);
            }
        }
    }

    private void modify_player_operation() throws Exception {
        if(playerData.getPlayer_data() == null){
            PlayerMenu.message("Empty Map");
            return;
        }
        int ID = PlayerMenu.ID_input_UI();
        if(!playerData.containsKey(ID)) throw new Exception("Player does not exist");
        Player player = playerData.getFrom_Map(ID);
        switch(PlayerMenu.modify_player_menu()){
            // case 1 is linked to case 2, because after changing region the server has to be changed too.
            case "Region": player.setRegion(PlayerMenu.region_chooser(playerData));
            case "Server": player.setServer(PlayerMenu.server_chooser(playerData, player.getRegion())); break;
            case "Name": player.setName(PlayerMenu.name_input_UI()); break;
            case "ALL":
                player.setRegion(PlayerMenu.region_chooser(playerData));
                player.setServer(PlayerMenu.server_chooser(playerData, player.getRegion()));
                player.setName(PlayerMenu.name_input_UI());
                break;
        }
        playerData.putIn_Map(ID, player);
        playerData.setFile_changed(true);
        PlayerMenu.message("Modify");
    }

    private void create_player() throws Exception {
        Player player = new Player();
        player.setRegion(PlayerMenu.region_chooser(playerData));
        player.setServer(PlayerMenu.server_chooser(playerData, player.getRegion()));
        player.setID(create_ID());
        player.setName(PlayerMenu.name_input_UI());
        if(playerData.isPlayer_Valid(player)) {
            if(playerData.getPlayer_data() == null){
                playerData.setPlayer_data(new HashMap<>());
            }
            playerData.getPlayer_data().put(player.getID(), player);
            playerData.setFile_changed(true);
        }else{
            throw new Exception("Player data is invalid");
        }
    }

    private int create_ID() throws Exception {
        while (true) {
            try {
                int ID = PlayerMenu.ID_input_UI();
                if (playerData.containsKey(ID)) {
                    throw new Exception("ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                PlayerMenu.message("ID Format");
            }
        }
    }

    private void delete_player() throws Exception {
        if(playerData.getPlayer_data() == null){
            PlayerMenu.message("Empty Map");
            return;
        }
        int ID = PlayerMenu.ID_input_UI();
        if(!playerData.containsKey(ID)) throw new Exception("Player does not exist");
        playerData.getPlayer_data().remove(ID);
        playerData.setFile_changed(true);
    }

    private void export_menu(FileManager fileManager) throws Exception {
        if(playerData.getPlayer_data() == null){
            PlayerMenu.message("Empty Map");
            return;
        }
        String target_extension = extension_control();
        fileManager.exportData(target_extension,playerData);
    }


}
