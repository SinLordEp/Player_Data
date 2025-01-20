package data.file;

/**
 * @author SIN
 */

public enum FileType {
    NONE, TXT, DAT, XML;

    public static FileType fromString(String input) {
        for (FileType fileType : FileType.values()) {
            if (input.equalsIgnoreCase(fileType.toString())) {
                return fileType;
            }
        }
        throw new IllegalArgumentException("Unknown file type: " + input);
    }
}
