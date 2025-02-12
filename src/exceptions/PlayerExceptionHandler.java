package exceptions;

import GUI.LogStage;
import Interface.EventListener;
import Interface.VerifiedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author SIN
 */
public class PlayerExceptionHandler {
    private static final PlayerExceptionHandler INSTANCE = new PlayerExceptionHandler();
    private final List<EventListener<TreeMap<Integer, VerifiedEntity>>> listeners = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(PlayerExceptionHandler.class);

    @FunctionalInterface
    public interface ExceptionWithReturn<T> {
        T run() throws Exception;
    }

    @FunctionalInterface
    public interface ExceptionWithoutReturn {
        void run() throws Exception;
    }

    public static PlayerExceptionHandler getInstance() {
        return INSTANCE;
    }

    public <T> T handle(ExceptionWithReturn<T> function, String className, String... playerTextSubType) {
        boolean success = true;
        String message = "Class&Function: %s - ".formatted(className);
        try{
            if(playerTextSubType.length == 1){
                logger.info("Method with return {} - Processing", className);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing");
            }else {
                logger.info("Method with return {} - Processing {}", className, playerTextSubType[1]);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing", playerTextSubType[1]);
            }
            return function.run();
        } catch (ConfigErrorException e){
            success = false;
            message += "Failed to read configuration.\nCause: " + e.getMessage();
        } catch (DatabaseException e){
            success = false;
            message += "Failed to communicate with database.\nCause: " + e.getMessage();
        } catch (DataCorruptedException e){
            success = false;
            message += "Data imported is corrupted.";
        } catch (DataTypeException e){
            success = false;
            message += "Data type is not supported.";
        } catch (FileManageException e){
            success = false;
            message += "Failed to manage file.\nCause: " + e.getMessage();
        } catch (HttpPhpException e){
            success = false;
            message += "Failed to communicate with PHP server.\nCause: " + e.getMessage();
        } catch (ObjectDBException e){
            success = false;
            message += "Failed to communicate with ObjectDB database.\nCause: " + e.getMessage();
        } catch (OperationCancelledException e){
            notifyLog(LogStage.INFO, "operation_cancelled");
        } catch (OperationException e){
            success = false;
            message += "Failed to proceed current operation.\nCause: " + e.getMessage();
        } catch (IOException e) {
            success = false;
            message += "IO exception has occurred.\nCause: " + e.getMessage();
        } catch (Exception e){
            success = false;
            message += "Undefined exception occurred.\nCause: " + e.getMessage();
        } finally {
            if (!success) {
                logger.error(message);
                notifyLog(LogStage.FAIL, playerTextSubType[0] + "_fail");
            } else{
                logger.info("Method with return {} - Success", className);
                notifyLog(LogStage.PASS, playerTextSubType[0] + "_pass");
            }
        }
        return null;
    }

    public void handle(ExceptionWithoutReturn function, String className, String... playerTextSubType) {
        boolean success = false;
        String message = "Class&Function: %s, ".formatted(className);

        try{
            if(playerTextSubType.length == 1){
                logger.info("Method without return {} - Processing", className);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing");
            }else {
                logger.info("Method without return {} - Processing {}", className, playerTextSubType[1]);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing", playerTextSubType[1]);
            }
            function.run();
            if(!"dataSourceChooser".equals(playerTextSubType[0]) && !"playerInfo".equals(playerTextSubType[0]) && !"default_database".equals(playerTextSubType[0])) {
                notifyLog(LogStage.PASS, playerTextSubType[0] + "_pass");
            }
            success = true;
        } catch (ConfigErrorException e){
            message += "Failed to read configuration.\nCause: " + e.getMessage();
        } catch (DatabaseException e){
            message += "Failed to communicate with database.\nCause: " + e.getMessage();
        } catch (DataCorruptedException e){
            message += "Data imported is corrupted.";
        } catch (DataTypeException e){
            message += "Data type is not supported.";
        } catch (FileManageException e){
            message += "Failed to manage file.\nCause: " + e.getMessage();
        } catch (HttpPhpException e){
            message += "Failed to communicate with PHP server.\nCause: " + e.getMessage();
        } catch (ObjectDBException e){
            message += "Failed to communicate with ObjectDB database.\nCause: " + e.getMessage();
        } catch (OperationCancelledException e){
            notifyLog(LogStage.INFO, "operation_cancelled");
            success = true;
        } catch (OperationException e){
            message += "Failed to proceed current operation.\nCause: " + e.getMessage();
        } catch (Exception e){
            message += "Undefined exception occurred.\nCause: " + e.getMessage();
        }finally{
            if (!success) {
                logger.error(message);
                notifyLog(LogStage.FAIL, playerTextSubType[0] + "_fail");
            } else{
                logger.info("Method without return {} - Success", className);
            }
        }
    }

    public void addListener(EventListener<TreeMap<Integer, VerifiedEntity>> listener){
        listeners.add(listener);
    }

    private void notifyLog(LogStage stage, String... message){
        for(EventListener<TreeMap<Integer, VerifiedEntity>> listener : listeners){
            listener.onLog(stage, message);
        }
    }
}
