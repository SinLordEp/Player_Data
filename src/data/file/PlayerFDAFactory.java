package data.file;

import Interface.PlayerFDA;
import exceptions.FileManageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author SIN
 */

public class PlayerFDAFactory {
    private static final PlayerFDAFactory INSTANCE = new PlayerFDAFactory();
    private final HashMap<FileType, PlayerFDA> playerFDAHashMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(PlayerFDAFactory.class);

    public static PlayerFDAFactory getInstance() {
        return INSTANCE;
    }
    private PlayerFDAFactory() {}

    public PlayerFDA getPlayerFDA(FileType fileType) {
        logger.info("Get playerFDA for {}", fileType);
        return playerFDAHashMap.computeIfAbsent(fileType, _ ->{
            try{
                logger.info("Creating playerFDA instance for {}", fileType);
                String classPackagePath = "data.file.%s".formatted(fileType.getClassName());
                Class<?> tempClass = Class.forName(classPackagePath);
                if(!PlayerFDA.class.isAssignableFrom(tempClass)) {
                    throw new IllegalArgumentException(classPackagePath + " does not implement PlayerFDA");
                }
                return (PlayerFDA) tempClass.getDeclaredConstructor().newInstance();
            }catch(Exception e) {
                throw new FileManageException("PlayerFDA could not be instantiated");
            }
        });
    }

}
