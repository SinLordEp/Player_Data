package Interface;

import model.DataInfo;

/**
 * @author SIN
 */
@FunctionalInterface
public interface DataInfoCallback {
    void onSubmit(DataInfo dataInfo);
}
