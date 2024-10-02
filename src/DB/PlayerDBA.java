package DB;

import model.Player;

import java.sql.*;
import java.util.TreeMap;

public class PlayerDBA implements GeneralDBA<TreeMap<?,?>, Player, Integer> {
    private String URL = "jdbc:mysql://localhost:3306/person";
    private String user = "root";
    private String password = "root";
    //private String table = "player";
    private Connection connection;

    public PlayerDBA() throws SQLException {
        connect();
    }

    @Override
    public boolean connected() {
        return connection != null;
    }

    @Override
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, user, password);
    }

    @Override
    public TreeMap<Integer, Player> read() throws SQLException {
        String query = "Select id_player, region, server, name from player";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        TreeMap<Integer, Player> player_map = new TreeMap<>();
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
        statement.executeUpdate(query);
    }

    public void add(Player player) throws SQLException {
        String query = "Insert into player (id_player, region, server, name) VALUES (?, ?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, player.getID());
        preparedStatement.setString(2, player.getRegion());
        preparedStatement.setString(3, player.getServer());
        preparedStatement.setString(4, player.getName());
        preparedStatement.executeUpdate();
    }

    public void modify(Player player) throws SQLException {
        String query = "UPDATE player set region = ?, server = ?, name = ? where id_player = ? ()";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(4, player.getID());
        preparedStatement.setString(1, player.getRegion());
        preparedStatement.setString(2, player.getServer());
        preparedStatement.setString(3, player.getName());
        preparedStatement.executeUpdate();
    }

    public void delete(Integer ID) throws SQLException {
        String query = "DELETE from player where id_player = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, ID);
        preparedStatement.executeUpdate();
    }

}

