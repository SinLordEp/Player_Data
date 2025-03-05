package Interface;

import exceptions.ConfigErrorException;

/**
 * @author SIN
 */
public interface GeneralControl {
    GeneralControl initialize() throws ConfigErrorException;
    void run();
    void onWindowClosing();
}
