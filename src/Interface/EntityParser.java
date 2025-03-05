package Interface;

import java.util.TreeMap;

/**
 * @author SIN
 */
public interface EntityParser {
    ParserCallBack<?, TreeMap<Integer, VerifiedEntity>> parseAll(Object dataType);
    ParserCallBack<?, VerifiedEntity> serializeOne(Object dataType);
    ParserCallBack<?, TreeMap<Integer, VerifiedEntity>> serializeAll(Object dataType);
}
