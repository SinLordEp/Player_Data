package control;

import GUI.GeneralMenu;
import GUI.PlayerMenu;
import data.GeneralDataAccess;
import data.PlayerDataAccess;
import model.Player;
import file.OperationCanceledException;

import java.util.HashMap;

public class PlayerControl implements GeneralControl {
    private PlayerDataAccess PlayerDA;
    @Override
    public void run() {
        this.PlayerDA = new PlayerDataAccess();
        while (PlayerDA.getFile_path() == null){
            try{
                switch (PlayerMenu.file_menu()){
                    case "Create new storage file":
                        PlayerDA.setFile_path(GeneralDataAccess.new_path_builder());
                        PlayerDA.write();
                        PlayerDA.setFile_changed(true);
                        break;
                    case "Read from existed file":
                        PlayerDA.setFile_path(GeneralDataAccess.get_path(GeneralDataAccess.choose_extension()));
                        PlayerDA.setFile_changed(true);
                        break;
                }
                PlayerDA.update_changes();
                if(PlayerDA.isPlayerMap_Valid()){
                    break;
                }else{
                    GeneralMenu.message_popup("Player data corrupted");
                    return;
                }
            }catch (OperationCanceledException e) {
                PlayerMenu.exception_message(e);
                return;
            } catch (Exception e) {
                PlayerMenu.exception_message(e);
            }
        }
        operation_control();
    }


    private void operation_control(){
        while (true){
            try{
                switch(PlayerMenu.operation_menu(PlayerDA.getFile_path())){
                    case "Show Data": PlayerDA.print_person(); break;
                    case "Modify data": modify_operation(); break;
                    case "Export data": export_menu(); break;
                }
            }catch (OperationCanceledException e) {
                PlayerMenu.exception_message(e);
                return;
            } catch (Exception e) {
                PlayerMenu.exception_message(e);
            }
        }
    }

    private void modify_operation(){
        while(true){
            try {
                switch(PlayerMenu.modify_menu()){
                    case "Create new Player": create_player(); break;
                    case "Modify Player data": modify_player_operation(); break;
                    case "Delete Player": delete_player(); break;
                }
                PlayerDA.update_changes();
                return;
            }catch (OperationCanceledException e) {
                PlayerMenu.exception_message(e);
                return;
            }catch (Exception e) {
                PlayerMenu.exception_message(e);
            }
        }
    }

    private void modify_player_operation() throws Exception {
        if(PlayerDA.getPlayer_data() == null){
            PlayerMenu.message("Empty Map");
            return;
        }
        int ID = PlayerMenu.ID_input_UI();
        if(!PlayerDA.containsKey(ID)) throw new Exception("Player does not exist");
        switch(PlayerMenu.modify_player_menu()){
            // case 1 is linked to case 2, because after changing region the server has to be changed too.
            case "Region": PlayerDA.getFrom_Map(ID).setRegion(PlayerMenu.region_chooser(PlayerDA.getRegion_list()));
            case "Server": PlayerDA.getFrom_Map(ID).setServer(PlayerMenu.server_chooser(PlayerDA.getServer_list(PlayerDA.getFrom_Map(ID).getRegion()))); break;
            case "Name": PlayerDA.getFrom_Map(ID).setName(GeneralMenu.universalInput("Enter player name: ")); break;
            case "ALL":
                PlayerDA.getFrom_Map(ID).setRegion(PlayerMenu.region_chooser(PlayerDA.getRegion_list()));
                PlayerDA.getFrom_Map(ID).setServer(PlayerMenu.server_chooser(PlayerDA.getServer_list(PlayerDA.getFrom_Map(ID).getRegion())));
                PlayerDA.getFrom_Map(ID).setName(GeneralMenu.universalInput("Enter player name: "));
                break;
        }
        PlayerDA.setFile_changed(true);
        PlayerMenu.message("Modify");
    }

    private void create_player() throws Exception {
        Player player = new Player();
        player.setRegion(PlayerMenu.region_chooser(PlayerDA.getRegion_list()));
        player.setServer(PlayerMenu.server_chooser(PlayerDA.getServer_list(player.getRegion())));
        player.setID(create_ID());
        player.setName(GeneralMenu.universalInput("Enter player name: "));
        if(PlayerDA.isPlayer_Valid(player)) {
            if(PlayerDA.getPlayer_data() == null){
                PlayerDA.setPlayer_data(new HashMap<>());
            }
            PlayerDA.getPlayer_data().put(player.getID(), player);
            PlayerDA.setFile_changed(true);
        }else{
            throw new Exception("Player data is invalid");
        }
    }

    private int create_ID() throws Exception {
        while (true) {
            try {
                int ID = PlayerMenu.ID_input_UI();
                if (PlayerDA.containsKey(ID)) {
                    throw new Exception("ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                PlayerMenu.message("ID Format");
            }
        }
    }

    private void delete_player() throws Exception {
        if(PlayerDA.getPlayer_data() == null){
            PlayerMenu.message("Empty Map");
            return;
        }
        int ID = PlayerMenu.ID_input_UI();
        if(!PlayerDA.containsKey(ID)) throw new Exception("Player does not exist");
        PlayerDA.getPlayer_data().remove(ID);
        PlayerDA.setFile_changed(true);
    }

    private void export_menu() throws Exception {
        if(PlayerDA.getPlayer_data() == null){
            PlayerMenu.message("Empty Map");
        }else{
            PlayerDA.export();
        }
    }


}
