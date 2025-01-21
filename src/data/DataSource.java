package data;

/**
 * @author SIN
 */

public enum DataSource {
    NONE("none"),
    FILE("none"),
    DATABASE("DataBaseDBA"),
    HIBERNATE("HibernateDBA"),
    PHP("none"),
    OBJECTDB("ObjectDBA");

    private final String className;

    DataSource(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
