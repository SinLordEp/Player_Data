package control;

import GUI.GeneralMenu;
import GUI.Player.PlayerUI;
import GUI.PlayerMenu;
import data.GeneralDataAccess;
import data.PlayerDataAccess;
import model.Player;
import main.OperationCanceledException;

import javax.swing.*;

public class PlayerControl implements GeneralControl {
    private PlayerDataAccess PlayerDA;
    @Override
    public void run() throws Exception {
        PlayerDA = new PlayerDataAccess();
        while (PlayerDA.getPlayer_map() == null){
            try{
                switch (PlayerMenu.run_menu()){
                    case "Create new storage file":
                        PlayerDA.setFile_path(GeneralDataAccess.new_path_builder());
                        PlayerDA.write();
                        PlayerDA.setData_changed(true);
                        break;
                    case "Read from existed file":
                        PlayerDA.setFile_path(GeneralDataAccess.get_path(GeneralDataAccess.choose_extension()));
                        break;
                    case "Read from DataBase":
                        PlayerDA.setDB(true);
                        break;
                }
                PlayerDA.refresh();
                break;
            }catch (OperationCanceledException e) {
                exception_message(e);
                return;
            } catch (Exception e) {
                exception_message(e);
            }
        }
        operation_control();
    }

    private void operation_control(){
        javax.swing.SwingUtilities.invokeLater(() -> {
            PlayerUI playerUI = new PlayerUI(PlayerDA.getPlayer_map());
            JFrame frame = new JFrame("Player Menu");
            frame.setContentPane(playerUI.getMain_panel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.pack();
            frame.setVisible(true);
        });
        while (true){
            try{
                Thread.sleep(5000);
                System.out.println("running");
                /*switch(PlayerMenu.operation_menu(PlayerDA.isDBConnected())){
                    case "Show Data": PlayerDA.print_person(); break;
                    case "Modify data": modify_control(); break;
                    case "Export data": export_control(); break;
                }*/

            }catch (OperationCanceledException e) {
                exception_message(e);
                return;
            } catch (Exception e) {
                exception_message(e);
            }
        }
    }

    private void modify_control(){
        while(true){
            try {
                switch(PlayerMenu.modify_menu()){
                    case "Create new Player": create_player_control(); break;
                    case "Modify Player data": modify_player_control(); break;
                    case "Delete Player": delete_control(); break;
                }
                PlayerDA.refresh();
                return;
            }catch (OperationCanceledException e) {
                exception_message(e);
                return;
            }catch (Exception e) {
                exception_message(e);
            }
        }
    }

    private void modify_player_control() throws Exception {
        if(PlayerDA.isEmpty()){
            message("Empty Map");
            return;
        }
        Player player = PlayerDA.pop(PlayerMenu.ID_input_UI());
        switch(PlayerMenu.modify_player_menu()){
            // case 1 is linked to case 2, because after changing region the server has to be changed too.
            case "Region": player.setRegion(PlayerMenu.region_chooser(PlayerDA.getRegion_list()));
            case "Server": player.setServer(PlayerMenu.server_chooser(PlayerDA.getServer_list(player.getRegion()))); break;
            case "Name": player.setName(GeneralMenu.universalInput("Enter player name: ")); break;
            case "ALL":
                player.setRegion(PlayerMenu.region_chooser(PlayerDA.getRegion_list()));
                player.setServer(PlayerMenu.server_chooser(PlayerDA.getServer_list(player.getRegion())));
                player.setName(GeneralMenu.universalInput("Enter player name: "));
                break;
        }
        GeneralMenu.message_popup(PlayerDA.update(player));
    }

    private void create_player_control() throws Exception {
        Player player = new Player();
        player.setRegion(PlayerMenu.region_chooser(PlayerDA.getRegion_list()));
        player.setServer(PlayerMenu.server_chooser(PlayerDA.getServer_list(player.getRegion())));
        player.setID(create_ID_control());
        player.setName(GeneralMenu.universalInput("Enter player name: "));
        GeneralMenu.message_popup(PlayerDA.add(player));
    }

    private int create_ID_control() throws Exception {
        while (true) {
            try {
                int ID = PlayerMenu.ID_input_UI();
                if (PlayerDA.containsKey(ID)) {
                    throw new Exception("ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                message("ID Format");
            }
        }
    }

    private void delete_control() throws Exception {
        int ID = PlayerMenu.ID_input_UI();
        GeneralMenu.message_popup(PlayerDA.delete(ID));
    }

    private void export_control() throws Exception {
        if(PlayerDA.isEmpty()){
            message("Empty Map");
        }else{
            switch (PlayerMenu.export_menu()){
                case "Export to file": PlayerDA.export(); break;
                case "Export all to database (Not recommended)": PlayerDA.export_DB(); break;
            }
        }
    }

    public static void message(String msg_type){
        GeneralMenu.message_popup(switch (msg_type){
            case "Empty Map" -> "No player data registered";
            case "ID Format" -> "ID format incorrect";
            case "Modify" -> "Modification completed";
            default -> "Unhandled or unknown error";
        });
    }

    public static void exception_message(Exception e){
        if(e instanceof OperationCanceledException){
            GeneralMenu.message_popup("Operation canceled");
        }else{
            GeneralMenu.message_popup(e.getMessage());
        }
    }

}
