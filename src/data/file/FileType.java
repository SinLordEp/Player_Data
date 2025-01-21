package data.file;

/**
 * @author SIN
 */

public enum FileType {
    NONE("none"),
    TXT("TextFDA"),
    DAT("DatFDA"),
    XML("XmlFDA");

    private final String className;

    FileType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
