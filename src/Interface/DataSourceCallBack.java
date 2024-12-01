package Interface;

import GUI.GeneralText;

/**
 * @author SIN
 */
public interface DataSourceCallBack<T, R> {
    void onSubmit(T datasource, R datatype);
    default void onCancel(){
        GeneralText.getDialog().popup("operation_cancelled");
    }
}
