package data.database;

import Interface.ParserCallBack;
import Interface.GeneralCRUD;
import data.DataOperation;
import exceptions.DatabaseException;
import model.DataInfo;
import model.Region;
import model.Server;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SIN
 */
public class DataBaseCRUD implements GeneralCRUD<DataInfo> {
    private Connection connection = null;
    private DataInfo dataInfo;
    @Override
    public GeneralCRUD<DataInfo> prepare(DataInfo dataInfo) throws DatabaseException {
        try {
            switch (dataInfo.getDialect()){
                case MYSQL:
                    connection = DriverManager.getConnection("%s:%s/%s".formatted(
                                    dataInfo.getUrl(),
                                    dataInfo.getPort(),
                                    dataInfo.getDatabase()),
                            dataInfo.getUser(),
                            dataInfo.getPassword());
                    break;
                case SQLITE:
                    connection = DriverManager.getConnection(dataInfo.getUrl());
                    break;
            }
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
        if(connection != null){
            this.dataInfo = dataInfo;
            return this;
        }else{
            throw new DatabaseException("Connection is null");
        }
    }

    @Override
    public void release() {
        connection = null;
    }

    /**
     * Reads all player parser from the database and returns it as a {@code TreeMap}.
     * This method executes a SQL query to retrieve player records, constructs {@code Player} objects
     * for each record, and maps them by their IDs.
     * It logs the process of reading players from the database and handles any {@code SQLException}
     * encountered during the operation by throwing a {@code DatabaseException}.
     * <p>
     * The method utilizes {@code Player.setID}, {@code Player.setName}, {@code Player.setRegion},
     * and {@code Player.setServer} to populate player attributes. It also creates associated objects
     * like {@code Region} and {@code Server} for each player.
     *
     * @return a {@code TreeMap<Integer, Player>} containing the player parser retrieved from the database,
     * where the keys are player IDs and the values are {@code Player} objects.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <R,U> GeneralCRUD<DataInfo> read(ParserCallBack<R,U> parser, DataOperation operation, U dataMap) {
        if(connection == null){
            throw new DatabaseException("Database is not connected");
        }
        String query = "SELECT * FROM %s".formatted(dataInfo.getTable());
        try(PreparedStatement statement = connection.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
               parser.parse((R)resultSet, operation, dataMap);
            }
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new DatabaseException(ex.getMessage());
            }
            throw new DatabaseException("Data rolled back with cause: " + e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation operation, U object) {
        String query = switch (operation){
            case ADD -> dataInfo.getQueryADD();
            case MODIFY -> dataInfo.getQueryModify();
            case DELETE -> dataInfo.getQueryDelete();
        };
        try(PreparedStatement statement = connection.prepareStatement(query)){
            parser.parse((R)statement, operation, object);
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
        return this;
    }

    /**
     * Reads the region-server configuration from the database using a query.
     * The method retrieves regions and their associated servers,
     * mapping each region to an array of servers assigned to it.
     * It uses a Hibernate session to execute the query and processes the results
     * to construct the region-server mapping.
     * This method closes the Hibernate session and disconnects the data source
     * using {@code disconnect(DataSource.HIBERNATE)} after reading the data.
     *
     * @return a {@code HashMap<Region, Server[]>} where each {@code Region} is a key,
     *         mapped to an array of {@code Server} instances that belong to that region.
     */
    public static HashMap<Region, Server[]> readRegionServer(DataInfo dataInfo) throws DatabaseException {
        HashMap<Region, List<Server>> regionServerListMap = new HashMap<>();
        HashMap<Region, Server[]> regionServerMap = new HashMap<>();
        String query = "SELECT r.name_region AS region_name, s.name_server AS server_name " +
                "FROM region r " +
                "JOIN server s ON r.name_region = s.region";
        try (Connection connection = DriverManager.getConnection(dataInfo.getUrl(),
                dataInfo.getUser(),
                dataInfo.getPassword());
             PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Region region = new Region(resultSet.getString("region_name"));
                Server server = new Server(resultSet.getString("server_name"), region);
                regionServerListMap.computeIfAbsent(region, _ -> new ArrayList<>()).add(server);
            }

            for (Map.Entry<Region, List<Server>> entry : regionServerListMap.entrySet()) {
                regionServerMap.put(entry.getKey(), entry.getValue().toArray(new Server[0]));
            }
        } catch (SQLException e) {
            throw new DatabaseException(e.getMessage());
        }
        return regionServerMap;
    }

}
