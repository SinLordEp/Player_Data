package data.file;

/**
 * @author SIN
 */

public enum FileType {
    NONE("none"),
    TXT("TextPlayerCRUD"),
    DAT("DatPlayerCRUD"),
    XML("XmlPlayerCRUD");

    private final String className;

    FileType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
