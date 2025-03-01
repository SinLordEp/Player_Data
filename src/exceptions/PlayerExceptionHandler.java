package exceptions;

import GUI.LogStage;
import Interface.EventListener;
import Interface.VerifiedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try{
            if(playerTextSubType.length == 1){
                logger.info("Method with return {} - Processing", className);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing");
            }else {
                logger.info("Method with return {} - Processing {}", className, playerTextSubType[1]);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing", playerTextSubType[1]);
            }
            return function.run();
        }catch (OperationCancelledException e){
            notifyLog(LogStage.INFO, "operation_cancelled");
        }catch (Exception e){
            success = false;
            handleException(e, className, playerTextSubType);
        } finally {
            if (success) {
                logger.info("Method with return {} - Success", className);
                notifyLog(LogStage.PASS, playerTextSubType[0] + "_pass");
            }
        }
        return null;
    }

    public void handle(ExceptionWithoutReturn function, String className, String... playerTextSubType) {
        try{
            if(playerTextSubType.length == 1){
                logger.info("Method without return {} - Processing", className);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing");
            }else {
                logger.info("Method without return {} - Processing {}", className, playerTextSubType[1]);
                notifyLog(LogStage.ONGOING, playerTextSubType[0] + "_ongoing", playerTextSubType[1]);
            }
            function.run();
            notifyLog(LogStage.PASS, playerTextSubType[0] + "_pass");
            logger.info("Method without return {} - Success", className);
        }catch (OperationCancelledException e){
            notifyLog(LogStage.INFO, "operation_cancelled");
        }catch (Exception e){
            handleException(e, className, playerTextSubType);
        }
    }

    private void handleException(Exception e, String className, String... playerTextSubType){
        String message = "Class&Function: %s, ".formatted(className);
        switch (e) {
            case ConfigErrorException _ -> message += "Failed to read configuration.\nCause: " + e.getMessage();
            case DatabaseException _ -> message += "Failed to communicate with database.\nCause: " + e.getMessage();
            case DataCorruptedException _ -> message += "Data imported is corrupted.";
            case DataTypeException _ -> message += "Data type is not supported.";
            case FileManageException _ -> message += "Failed to manage file.\nCause: " + e.getMessage();
            case HttpPhpException _ -> message += "Failed to communicate with PHP server.\nCause: " + e.getMessage();
            case ObjectDBException _ -> message += "Failed to communicate with ObjectDB database.\nCause: " + e.getMessage();
            case OperationException _ -> message += "Failed to proceed current operation.\nCause: " + e.getMessage();
            default -> message += "Undefined exception occurred.\nCause: " + e.getMessage();
        }
        logger.error(message);
        notifyLog(LogStage.FAIL, playerTextSubType[0] + "_fail");
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
