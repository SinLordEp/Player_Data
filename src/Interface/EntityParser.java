package Interface;

import data.DataOperation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.TreeMap;

/**
 * @author SIN
 */
public interface EntityParser {
    void parseResultSet(ResultSet resultSet, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap);
    void entityToUpdateStatement(PreparedStatement statement, DataOperation operation, VerifiedEntity entity);
    void parseList(List<VerifiedEntity> list, DataOperation operation, TreeMap<Integer, VerifiedEntity> dataMap);
}
