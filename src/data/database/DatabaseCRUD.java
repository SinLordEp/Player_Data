package data.database;

import Interface.GeneralCRUD;
import Interface.ParserCallBack;
import data.DataOperation;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.DataInfo;
import model.Region;
import model.Server;

import java.sql.*;
import java.util.*;

/**
 * @author SIN
 */
@SuppressWarnings("unused")
public class DatabaseCRUD implements GeneralCRUD<DataInfo> {
    private Connection connection = null;
    private final DataInfo dataInfo;

    public DatabaseCRUD(DataInfo dataInfo) {
        this.dataInfo = dataInfo;
    }

    @Override
    public GeneralCRUD<DataInfo> prepare() throws DatabaseException {
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
            return this;
        }else{
            throw new DatabaseException("Connection is null");
        }
    }

    @Override
    public void release() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DatabaseException("Error closing connection");
        }
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
    public <R,U> GeneralCRUD<DataInfo> read(ParserCallBack<R,U> parser, DataOperation dataOperation, U dataContainer) {
        if(connection == null){
            throw new DatabaseException("Database is not connected");
        }
        String query = switch (dataOperation){
            case READ -> "SELECT * FROM %s".formatted(dataInfo.getTable());
            case SEARCH -> "SELECT * FROM %s where id = %s".formatted(dataInfo.getTable(), ((TreeMap<?, ?>) dataContainer).firstKey());
            default -> throw new OperationException("Unexpected DataOperation for reading: " + dataOperation);
        };
        try(PreparedStatement statement = connection.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
               parser.parse((R)resultSet, dataOperation, dataContainer);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Cannot read from database. Cause: " + e.getMessage());
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R, U> GeneralCRUD<DataInfo> update(ParserCallBack<R, U> parser, DataOperation dataOperation, U dataContainer) {
        String query = switch (dataOperation){
            case ADD -> dataInfo.getQueryADD();
            case MODIFY -> dataInfo.getQueryModify();
            case DELETE -> dataInfo.getQueryDelete();
            default -> throw new OperationException("Unexpected DataOperation for updating: " + dataOperation);
        };
        try(PreparedStatement statement = connection.prepareStatement(query)){
            parser.parse((R)statement, dataOperation, dataContainer);
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
