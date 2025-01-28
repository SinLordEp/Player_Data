package Interface;

import GUI.Player.PlayerText;

/**
 * @author SIN
 */
public interface DataSourceCallBack<T, R> {
    void onSubmit(T datasource, R datatype);
    default void onCancel(){
        PlayerText.getDialog().popup("operation_cancelled");
    }
}
