package Interface;

import GUI.Player.PlayerText;

/**
 * @author SIN
 */
@FunctionalInterface
public interface CallBack<T> {
    void onSubmit(T object);
    default void onCancel(){
        PlayerText.getDialog().popup("operation_cancelled");
    }
}
