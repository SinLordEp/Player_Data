package data;

import Interface.GeneralCRUD;
import exceptions.OperationException;
import model.DataInfo;

import java.lang.reflect.InvocationTargetException;

import static main.principal.getProperty;

/**
 * @author SIN
 */
public class CRUDFactory {

    @SuppressWarnings("unchecked")
    public static GeneralCRUD<DataInfo> getCRUD(DataInfo dataInfo) {
        String classPackagePath = "data.%s".formatted(getProperty(dataInfo.getDataType().toString()));
        try {
            Class<GeneralCRUD<DataInfo>> tempClass = (Class<GeneralCRUD<DataInfo>>) Class.forName(classPackagePath);
            return tempClass.getDeclaredConstructor(DataInfo.class).newInstance(dataInfo);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new OperationException("%sCRUD could not be instantiated".formatted(dataInfo.getDataType()));
        }
    }
}
