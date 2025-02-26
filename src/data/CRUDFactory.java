package data;

import Interface.GeneralCRUD;
import exceptions.OperationException;
import model.DataInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class CRUDFactory {
    private final static HashMap<String, Function<DataInfo, GeneralCRUD<DataInfo>>> CRUD_INSTANCES = new HashMap<>();

    static {
        initializeCRUD();
    }

    @SuppressWarnings("unchecked")
    private static void initializeCRUD() {
        List<String> supportedCRUD = Arrays.asList(getProperty("supportedCRUD").split(","));
        supportedCRUD.forEach(dataType -> CRUD_INSTANCES.put(dataType, (dataInfo) ->{
            try {
                Class<?> tempClass = Class.forName("data.%s".formatted(getProperty(dataType)));
                if(GeneralCRUD.class.isAssignableFrom(tempClass)){
                    return (GeneralCRUD<DataInfo>) tempClass.getDeclaredConstructor(DataInfo.class).newInstance(dataInfo);
                }
                throw new OperationException("Unsupported CRUD type: " + dataType);
            } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new OperationException("%sCRUD could not be instantiated".formatted(dataType));
            }
        }));
    }

    public static GeneralCRUD<DataInfo> getCRUD(DataInfo dataInfo) {
        return CRUD_INSTANCES.get(dataInfo.getDataType().toString()).apply(dataInfo);
    }


}
