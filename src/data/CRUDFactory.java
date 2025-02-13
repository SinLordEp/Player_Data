package data;

import Interface.GeneralCRUD;
import data.file.FileType;
import data.http.PhpType;
import exceptions.DatabaseException;
import exceptions.OperationException;
import model.DataInfo;

import java.util.HashMap;

/**
 * @author SIN
 */
public class CRUDFactory {
    private final static CRUDFactory INSTANCE = new CRUDFactory();
    private final HashMap<Enum<?>, GeneralCRUD<?>> CRUDHashMap = new HashMap<>();

    public static CRUDFactory getInstance() {
        return INSTANCE;
    }

    private CRUDFactory() {}

    public GeneralCRUD<DataInfo> getCRUD(DataInfo dataInfo) {
        return switch(dataInfo.getDataType()){
            case FileType ignore -> getCRUD((FileType) dataInfo.getDataType());
            case PhpType ignore -> getCRUD((PhpType) dataInfo.getDataType());
            case DataSource ignore -> getCRUD((DataSource) dataInfo.getDataType());
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataInfo.getDataType());
        };
    }

    @SuppressWarnings("unchecked")
    private GeneralCRUD<DataInfo> getCRUD(DataSource dataSource){
        return (GeneralCRUD<DataInfo>) CRUDHashMap.computeIfAbsent(dataSource, _->{
            try {
                String classPackagePath = "data.database.%s".formatted(switch (dataSource){
                    case DATABASE -> "DatabaseCRUD";
                    case HIBERNATE -> "HibernateCRUD";
                    case OBJECTDB -> "ObjectDBCRUD";
                    case BASEX -> "BaseXCRUD";
                    case MONGO -> "MongoCRUD";
                    default -> throw new DatabaseException("Unknown database type: " + dataSource);
                });
                Class<?> tempClass = Class.forName(classPackagePath);
                return (GeneralCRUD<DataInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("CRUD could not be instantiated");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private GeneralCRUD<DataInfo> getCRUD(PhpType phpType){
        return (GeneralCRUD<DataInfo>) CRUDHashMap.computeIfAbsent(DataSource.PHP, _->{
            try {
                String classPackagePath = "data.http.%s".formatted("PhpCRUD");
                Class<?> tempClass = Class.forName(classPackagePath);
                return (GeneralCRUD<DataInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("CRUD could not be instantiated");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private GeneralCRUD<DataInfo> getCRUD(FileType fileType){
        return (GeneralCRUD<DataInfo>) CRUDHashMap.computeIfAbsent(fileType, _->{
            try {
                String classPackagePath = "data.file.%s".formatted((switch (fileType){
                    case TXT -> "TxtCRUD";
                    case XML -> "XmlCRUD";
                    case DAT -> "DatCRUD";
                    default -> throw new DatabaseException("Unknown file type: " + fileType);
                }));
                Class<?> tempClass = Class.forName(classPackagePath);
                return (GeneralCRUD<DataInfo>) tempClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new OperationException("CRUD could not be instantiated");
            }
        });
    }


}
