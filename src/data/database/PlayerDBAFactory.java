package data.database;

import Interface.PlayerDBA;
import data.DataSource;
import exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author SIN
 */
public class PlayerDBAFactory {
    private final static PlayerDBAFactory INSTANCE = new PlayerDBAFactory();
    private final HashMap<DataSource, PlayerDBA> playerDBAHashMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PlayerDBAFactory.class);

    public static PlayerDBAFactory getInstance() {
        return INSTANCE;
    }
    private PlayerDBAFactory() {}

    public PlayerDBA getDBA(DataSource dataSource) {
        logger.info("Getting PlayerDBA for {}", dataSource);
        return playerDBAHashMap.computeIfAbsent(dataSource, _ ->{
            try{
                logger.info("Creating PlayerDBA instance for {}", dataSource);
                String classPackagePath = "data.database.%s".formatted(dataSource.getClassName());
                Class<?> tempClass = Class.forName(classPackagePath);
                if(!PlayerDBA.class.isAssignableFrom(tempClass)) {
                    throw new IllegalArgumentException(classPackagePath + " does not implement PlayerDBA");
                }
                return (PlayerDBA) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new DatabaseException("PlayerDBA could not be instantiated");
            }
        });
    }

}
