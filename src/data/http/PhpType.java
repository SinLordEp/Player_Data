package data.http;

/**
 * @author SIN
 */

public enum PhpType {
    NONE("none"),
    JSON("JsonPlayerCRUD");

    private final String className;

    PhpType(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
