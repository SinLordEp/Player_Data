package data;

import Interface.PlayerCRUD;
import data.file.FileType;
import exceptions.DatabaseException;
import model.DatabaseInfo;

import java.util.HashMap;

/**
 * @author SIN
 */
public class PlayerCRUDFactory {
    private final static PlayerCRUDFactory INSTANCE = new PlayerCRUDFactory();
    private final HashMap<Enum<?>, PlayerCRUD<?>> playerCRUDHashMap = new HashMap<>();

    public static PlayerCRUDFactory getInstance() {
        return INSTANCE;
    }

    private PlayerCRUDFactory() {}


    @SuppressWarnings("unchecked")
    public PlayerCRUD<DatabaseInfo> getCRUD(DataSource dataSource){
        return (PlayerCRUD<DatabaseInfo>) playerCRUDHashMap.computeIfAbsent(dataSource, _->{
            try {
                String classPackagePath = "data.database.%s".formatted(dataSource.getClassName());
                Class<?> tempClass = Class.forName(classPackagePath);
                return (PlayerCRUD<DatabaseInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new DatabaseException("PlayerCRUD could not be instantiated");
            }
        });
    }

    @SuppressWarnings("unchecked")
    public PlayerCRUD<String> getCRUD(FileType fileType){
        return (PlayerCRUD<String>) playerCRUDHashMap.computeIfAbsent(fileType, _->{
            try {
                String classPackagePath = "data.file.%s".formatted(fileType.getClassName());
                Class<?> tempClass = Class.forName(classPackagePath);
                return (PlayerCRUD<String>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new DatabaseException("PlayerCRUD could not be instantiated");
            }
        });
    }

}
