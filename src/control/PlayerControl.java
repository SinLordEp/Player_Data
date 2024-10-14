package control;

import GUI.GeneralMenu;
import GUI.Player.PlayerUI;
import GUI.Player.PlayerMenu;
import data.GeneralDataAccess;
import data.PlayerDataAccess;
import model.Player;
import main.OperationCanceledException;

public class PlayerControl implements GeneralControl<PlayerDataAccess> {
    private PlayerDataAccess playerDA;
    @Override
    public void run() {
        PlayerUI playerUI = new PlayerUI(this);
        playerUI.run();
        while (true){
            try{
                Thread.sleep(5000);
                System.out.println("Operation UI is running");
            }catch (OperationCanceledException e) {
                exception_message(e);
                return;
            } catch (Exception e) {
                exception_message(e);
            }
        }
    }

    @Override
    public void setDA(GeneralDataAccess DA) {
        this.playerDA = (PlayerDataAccess) DA;
    }


    public void modify_player_control(int selected_player_id) throws Exception {
        Player player = playerDA.getPlayer_map().get(selected_player_id);
        switch(PlayerMenu.modify_player_menu()){
            // After changing region the server has to be changed too.
            case "Region": player.setRegion(PlayerMenu.region_chooser(playerDA.getRegion_list()));
            case "Server": player.setServer(PlayerMenu.server_chooser(playerDA.getServer_list(player.getRegion()))); break;
            case "Name": player.setName(GeneralMenu.universalInput("Enter player name: ")); break;
            case "ALL":
                player.setRegion(PlayerMenu.region_chooser(playerDA.getRegion_list()));
                player.setServer(PlayerMenu.server_chooser(playerDA.getServer_list(player.getRegion())));
                player.setName(GeneralMenu.universalInput("Enter player name: "));
                break;
        }
        GeneralMenu.message_popup(playerDA.update(player));
    }

    public void create_player_control() throws Exception {
        Player player = new Player();
        player.setRegion(PlayerMenu.region_chooser(playerDA.getRegion_list()));
        player.setServer(PlayerMenu.server_chooser(playerDA.getServer_list(player.getRegion())));
        player.setID(create_ID_control());
        player.setName(GeneralMenu.universalInput("Enter player name: "));
        GeneralMenu.message_popup(playerDA.add(player));
    }

    private int create_ID_control() throws Exception {
        while (true) {
            try {
                int ID = PlayerMenu.ID_input_UI();
                if (playerDA.containsKey(ID)) {
                    throw new Exception("ID already existed");
                } else return ID;
            } catch (NumberFormatException e) {
                PlayerMenu.error_message("ID Format");
            }
        }
    }

    public void delete_control(int selected_player_id) throws Exception {
        GeneralMenu.message_popup(playerDA.delete(selected_player_id));
    }

    public void export_control() throws Exception {
        if(playerDA.isEmpty()){
            PlayerMenu.error_message("Empty Map");
        }else{
            switch (PlayerMenu.export_menu()){
                case "Export to File": playerDA.export(); break;
                case "Overwrite Database": playerDA.export_DB(); break;
            }
        }
    }

    public static void exception_message(Exception e){
        if(e instanceof OperationCanceledException){
            GeneralMenu.message_popup("Operation canceled");
        }else{
            GeneralMenu.message_popup(e.getMessage());
        }
    }

    @Override
    public PlayerDataAccess getDA() {
        return playerDA;
    }

}
