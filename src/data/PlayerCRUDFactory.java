package data;

import Interface.PlayerCRUD;
import data.file.FileType;
import data.http.PhpType;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.DataInfo;

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

    public PlayerCRUD<DataInfo> getCRUD(DataInfo dataInfo) {
        return switch(dataInfo.getDataType()){
            case FileType ignore -> getCRUD((FileType) dataInfo.getDataType());
            case PhpType ignore -> getCRUD((PhpType) dataInfo.getDataType());
            case DataSource ignore -> getCRUD((DataSource) dataInfo.getDataType());
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataInfo.getDataType());
        };
    }

    @SuppressWarnings("unchecked")
    private PlayerCRUD<DataInfo> getCRUD(DataSource dataSource){
        return (PlayerCRUD<DataInfo>) playerCRUDHashMap.computeIfAbsent(dataSource, _->{
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
                return (PlayerCRUD<DataInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("PlayerCRUD could not be instantiated");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private PlayerCRUD<DataInfo> getCRUD(PhpType phpType){
        return (PlayerCRUD<DataInfo>) playerCRUDHashMap.computeIfAbsent(DataSource.PHP, _->{
            try {
                String classPackagePath = "data.http.%s".formatted(getProperty("playerPhpCRUD"));
                Class<?> tempClass = Class.forName(classPackagePath);
                return (PlayerCRUD<DataInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("PlayerCRUD could not be instantiated");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private PlayerCRUD<DataInfo> getCRUD(FileType fileType){
        return (PlayerCRUD<DataInfo>) playerCRUDHashMap.computeIfAbsent(fileType, _->{
            try {
                String classPackagePath = "data.file.%s".formatted(getProperty(switch (fileType){
                    case TXT -> "playerTxtCRUD";
                    case XML -> "playerXmlCRUD";
                    case DAT -> "playerDatCRUD";
                    default -> throw new DatabaseException("Unknown file type: " + fileType);
                }));
                Class<?> tempClass = Class.forName(classPackagePath);
                return (PlayerCRUD<DataInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("PlayerCRUD could not be instantiated");
            }
        });
    }


}
