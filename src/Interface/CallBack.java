package Interface;

import GUI.GeneralText;

/**
 * @author SIN
 */
@FunctionalInterface
public interface CallBack<T> {
    void onSubmit(T object);
    default void onCancel(){
        GeneralText.getDialog().popup("operation_cancelled");
    }
}
