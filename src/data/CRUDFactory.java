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
        List<String> supportedCRUD = Arrays.asList(getProperty("supportedCRUD").split(","));
        supportedCRUD.forEach(dataType -> CRUD_INSTANCES.put(dataType, (dataInfo) ->{
            try {
                Class<GeneralCRUD<DataInfo>> tempClass = (Class<GeneralCRUD<DataInfo>>) Class.forName("data.%s".formatted(getProperty(dataType)));
                return tempClass.getDeclaredConstructor(DataInfo.class).newInstance(dataInfo);
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
