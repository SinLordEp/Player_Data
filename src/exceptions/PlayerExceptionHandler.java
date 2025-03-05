package exceptions;

import GUI.LogStage;
import Interface.EventListener;
import Interface.ExceptionHandler;
import Interface.VerifiedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author SIN
 */
public class PlayerExceptionHandler implements ExceptionHandler {
    private static PlayerExceptionHandler INSTANCE = null;
    private final List<EventListener<TreeMap<Integer, VerifiedEntity>>> listeners = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(PlayerExceptionHandler.class);
    private final Map<Class<? extends Exception>, String> exceptionMessages = Map.of(
            ConfigErrorException.class,"Failed to read configuration.",
            DatabaseException.class, "Failed to communicate with database.",
            DataCorruptedException.class, "Data imported is corrupted.",
            DataTypeException.class, "Data type is not supported.",
            FileManageException.class, "Failed to manage file.",
            HttpPhpException.class, "Failed to communicate with PHP server.",
            OperationException.class, "Failed to proceed current operation.");


    public static PlayerExceptionHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerExceptionHandler();
        }
        return INSTANCE;
    }

    @Override
    public <T> T handle(ExceptionWithReturn<T> function, String className, String... textSubType) {
        boolean success = true;
        try{
            if(textSubType.length == 1){
                logger.info("Method with return {} - Processing", className);
                notifyLog(LogStage.ONGOING, textSubType[0] + "_ongoing");
            }else {
                logger.info("Method with return {} - Processing {}", className, textSubType[1]);
                notifyLog(LogStage.ONGOING, textSubType[0] + "_ongoing", textSubType[1]);
            }
            return function.run();
        }catch (OperationCancelledException e){
            notifyLog(LogStage.INFO, "operation_cancelled");
        }catch (Exception e){
            success = false;
            handleException(e, className, textSubType);
        } finally {
            if (success) {
                logger.info("Method with return {} - Finished", className);
                notifyLog(LogStage.PASS, textSubType[0] + "_pass");
            }
        }
        return null;
    }

    @Override
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
            logger.info("Method without return {} - Finished", className);
        }catch (OperationCancelledException e){
            notifyLog(LogStage.INFO, "operation_cancelled");
        }catch (Exception e){
            handleException(e, className, playerTextSubType);
        }
    }

    private void handleException(Exception e, String className, String... playerTextSubType){
        String message = "Class&Function: %s - %s\nCause: %s"
                .formatted(className, exceptionMessages.getOrDefault(e.getClass(), "Undefined exception occurred."), e.getMessage());
        logger.error(message);
        notifyLog(LogStage.FAIL, playerTextSubType[0] + "_fail");
    }

    @Override
    public void addListener(EventListener<TreeMap<Integer, VerifiedEntity>> listener){
        listeners.add(listener);
    }

    private void notifyLog(LogStage stage, String... message){
        for(EventListener<TreeMap<Integer, VerifiedEntity>> listener : listeners){
            listener.onLog(stage, message);
        }
    }
}
