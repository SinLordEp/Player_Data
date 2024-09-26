package DB;

import GUI.GeneralMenu;
import model.Player;

import java.sql.*;
import java.util.HashMap;

public class PlayerDBA implements GeneralDBA<HashMap<?,?>, Player, Integer> {
    private String URL = "jdbc:mysql://localhost:3306/person";
    private String user = "root";
    private String password = "root";
    private String table = "player";
    private Connection connection;

    @Override
    public void initialize() throws Exception {
        connect();
    }

    @Override
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, user, password);
    }

    @Override
    public HashMap<Integer, Player> read() throws SQLException {
        String query = "Select id_player, region, server, name from players";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        HashMap<Integer, Player> player_map = new HashMap<>();
        while (resultSet.next()){
            Player player = new Player();
            player.setID(resultSet.getInt("id_player"));
            player.setRegion(resultSet.getString("region"));
            player.setServer(resultSet.getString("server"));
            player.setName(resultSet.getString("name"));
            player_map.put(player.getID(), player);
        }
        return player_map;
    }

    public void wipe() throws SQLException {
        String query = "TRUNCATE TABLE player";
        Statement statement = connection.createStatement();
        statement.executeQuery(query);
    }

    public void add(Player player) throws SQLException {
        String query = "Insert into player (id, region, server, name) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, player.getID());
        preparedStatement.setString(2, player.getRegion());
        preparedStatement.setString(3, player.getServer());
        preparedStatement.setString(4, player.getName());
        preparedStatement.executeUpdate();
        GeneralMenu.message_popup("Player added to DB");
    }

    public void modify(Player player) throws SQLException {
        String query = "UPDATE player set region = ?, server = ?, name = ? where id = ? ()";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(4, player.getID());
        preparedStatement.setString(1, player.getRegion());
        preparedStatement.setString(2, player.getServer());
        preparedStatement.setString(3, player.getName());
        preparedStatement.executeUpdate();
        GeneralMenu.message_popup("Player update in DB");
    }

    public void delete(Integer ID) throws SQLException {
        String query = "DELETE from player where id = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();
        GeneralMenu.message_popup("Player added");
    }

}

