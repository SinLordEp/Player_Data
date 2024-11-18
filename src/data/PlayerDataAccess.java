package data;

import GUI.GeneralDialog;
import GUI.Player.PlayerDialog;
import data.database.PlayerDBA;
import main.OperationException;
import model.Player;
import data.file.PlayerFileReader;
import data.file.PlayerFileWriter;

import javax.management.openmbean.OpenDataException;
import java.util.HashMap;
import java.util.TreeMap;

public class PlayerDataAccess extends GeneralDataAccess {
    private TreeMap<Integer, Player> player_map = new TreeMap<>();
    private final HashMap<Player, DataOperation> changed_player_map = new HashMap<>();
    private final HashMap<String, String[]> region_server_map;
    private final String[] region_list;
    private final PlayerDBA playerDBA;
    private final PlayerFileReader fileReader;
    private final PlayerFileWriter fileWriter;

    public PlayerDataAccess() throws Exception {
        fileReader = new PlayerFileReader();
        fileWriter = new PlayerFileWriter();
        playerDBA = new PlayerDBA();
        region_server_map = PlayerFileReader.read_region_server();
        region_list = region_server_map.keySet().toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    public void read() {
        try {
            switch (dataSource){
                case FILE -> player_map = (TreeMap<Integer, Player>) fileReader.read(file_path);
                case MYSQL, SQLITE -> player_map = playerDBA.read();
            }
            if(player_map != null && !isDataValid()){
                throw new OpenDataException("Data is corrupted");
            }
        } catch (Exception e) {
            GeneralDialog.get().message("Failed to read data\n" + e.getMessage());
            player_map = new TreeMap<>();
            dataSource = DataSource.NONE;
        }
    }

    public void save(){
        try{
            switch (dataSource){
                case FILE -> fileWriter.write(file_path, player_map);
                case MYSQL, SQLITE -> playerDBA.update(changed_player_map);
            }
        } catch (Exception e) {
            GeneralDialog.get().message("Failed to save data\n" + e.getMessage());
        }
    }

    public void add() {
            Player player = new Player();
            player.setRegion(PlayerDialog
                    .get()
                    .selectionDialog("region_menu", region_list));
            player.setServer(PlayerDialog
                    .get()
                    .selectionDialog("server_menu", region_server_map.get(player.getRegion())));
            player.setID(createID());
            player.setName(PlayerDialog
                    .get()
                    .input("player_name"));
            switch(dataSource){
                //case FILE ->
                case MYSQL, SQLITE -> changed_player_map.put(player, DataOperation.ADD);
            }
            player_map.put(player.getID(), player);
            PlayerDialog.get().popup( "added_player");
    }

    private int createID() {
        while (true) {
            try {
                int ID = Integer.parseInt(PlayerDialog.get().input("id"));
                if (player_map.containsKey(ID)) {
                    throw new OperationException("ID already existed\n");
                } else return ID;
            } catch (NumberFormatException e) {
                PlayerDialog.get().popup("number_format_invalid");
            }
        }
    }

    public void modify(int selected_player_id) {

        Player player = player_map.get(selected_player_id);
        switch(PlayerDialog.get().selectionDialog("modify_player")){
            // After changing region the server has to be changed too.
            case 0: player.setRegion(PlayerDialog
                    .get()
                    .selectionDialog("region_menu", region_list));
            case 1: player.setServer(PlayerDialog
                    .get()
                    .selectionDialog("server_menu", region_server_map.get(player.getRegion())));
                break;
            case 2: player.setName(PlayerDialog
                    .get()
                    .input("player_name"));
                break;
            case 3:
                player.setRegion(PlayerDialog
                        .get()
                        .selectionDialog("region_menu", region_list));
                player.setServer(PlayerDialog
                        .get()
                        .selectionDialog("server_menu",region_server_map.get(player.getRegion())));
                player.setName(PlayerDialog
                        .get()
                        .input("player_name"));
                break;
        }
        switch(dataSource){
            //case FILE ->
            case MYSQL, SQLITE  -> changed_player_map.put(player, DataOperation.MODIFY);
        }
        player_map.put(player.getID(), player);
    }

    public void delete(int selected_player_id) {
        switch(dataSource){
            //case FILE ->
            case MYSQL, SQLITE -> changed_player_map.put(player_map.get(selected_player_id), DataOperation.DELETE);
        }
        player_map.remove(selected_player_id);
        PlayerDialog.get().popup( "deleted_player");
    }

    public void export() throws Exception {
        if(fileWriter != null){
            String target_extension = chooseExtension();
            String target_path = getPath("path");
            String target_name = PlayerDialog.get().input("new_file_name");
            target_path += "/" + target_name + target_extension;
            fileWriter.write(target_path, player_map);
        }else {
            throw new IllegalStateException("Writer is not initialized");
        }
    }

    public void exportDB(){
        if(!playerDBA.connected()){
            PlayerDialog.get().popup("db_not_connected");
            return;
        }
        playerDBA.update(DataOperation.EXPORT, player_map);
        PlayerDialog.get().popup("exported_db");
    }


    public boolean isEmpty(){
        return player_map == null;
    }

    public TreeMap<Integer, Player> getPlayerMap() {
        return player_map;
    }

    public boolean isPlayerInvalid(Player player){
        if(region_server_map == null){
            PlayerDialog.get().popup("region_server_null");
            return true;
        }
        if(!region_server_map.containsKey(player.getRegion())){
            PlayerDialog.get().popup("region_invalid");
            return true;
        }
        boolean server_valid = false;
        for(String server : region_server_map.get(player.getRegion())){
            if (server.equals(player.getServer())) {
                server_valid = true;
                break;
            }
        }
        if(!server_valid){
            PlayerDialog.get().popup("server_invalid");
            return true;
        }

        if(player.getID() <= 0){
            PlayerDialog.get().popup("id_invalid");
            return true;
        }

        if(player_map == null) return false;
        if(player.getName().isBlank()){
            PlayerDialog.get().popup("name_invalid");
            return true;
        }
        return false;
    }

    public boolean isDataValid(){
        if(player_map == null){
            PlayerDialog.get().popup("player_map_null");
            return true;
        }
        for(Player player : player_map.values()){
            if(isPlayerInvalid(player)){
                return false;
            }
        }
        return true;
    }

    public void configureDB(String URL, String port, String database, String user, String password) {
        playerDBA.setURL(URL + ":" + port + "/" + database);
        playerDBA.setUser(user);
        playerDBA.setPassword(password);
    }

    public void configureDB(String URL) {
        playerDBA.setURL(URL);
    }

    public boolean connectDB() {
        if(playerDBA.connect()){
            file_path = null;
            return true;
        }else return false;
    }

    public boolean disconnectDB(){
        if(dataSource.equals(DataSource.MYSQL) || dataSource.equals(DataSource.SQLITE)){
            save();
            player_map = new TreeMap<>();
        }
        return true;
    }
}
