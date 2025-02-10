package data;

import Interface.PlayerCRUD;
import data.file.FileType;
import data.http.PhpType;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.DatabaseInfo;

import java.util.HashMap;

import static main.principal.getProperty;

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
                String classPackagePath = "data.database.%s".formatted(getProperty(switch (dataSource){
                    case DATABASE -> "playerDatabaseCRUD";
                    case HIBERNATE -> "playerHibernateCRUD";
                    case OBJECTDB -> "playerObjectDBCRUD";
                    case BASEX -> "playerBaseXCRUD";
                    case MONGO -> "playerMongoCRUD";
                    default -> throw new DatabaseException("Unknown database type: " + dataSource);
                }));
                Class<?> tempClass = Class.forName(classPackagePath);
                return (PlayerCRUD<DatabaseInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("PlayerCRUD could not be instantiated");
            }
        });
    }

    @SuppressWarnings("unchecked")
    public PlayerCRUD<PhpType> getCRUD(){
        return (PlayerCRUD<PhpType>) playerCRUDHashMap.computeIfAbsent(DataSource.PHP, _->{
            try {
                String classPackagePath = "data.http.%s".formatted(getProperty("playerPhpCRUD"));
                Class<?> tempClass = Class.forName(classPackagePath);
                return (PlayerCRUD<PhpType>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("PlayerCRUD could not be instantiated");
            }
        });
    }

    @SuppressWarnings("unchecked")
    public PlayerCRUD<String> getCRUD(FileType fileType){
        return (PlayerCRUD<String>) playerCRUDHashMap.computeIfAbsent(fileType, _->{
            try {
                String classPackagePath = "data.file.%s".formatted(getProperty(switch (fileType){
                    case TXT -> "playerTxtCRUD";
                    case XML -> "playerXmlCRUD";
                    case DAT -> "playerDatCRUD";
                    default -> throw new DatabaseException("Unknown file type: " + fileType);
                }));
                Class<?> tempClass = Class.forName(classPackagePath);
                return (PlayerCRUD<String>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("PlayerCRUD could not be instantiated");
            }
        });
    }


}
