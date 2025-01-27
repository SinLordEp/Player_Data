package data;

/**
 * @author SIN
 */

public enum DataSource {
    NONE("none"),
    FILE("none"),
    DATABASE("DataBasePlayerCRUD"),
    HIBERNATE("HibernatePlayerCRUD"),
    PHP("none"),
    OBJECTDB("ObjectDBPlayerCRUD"),
    BASEX("BaseXPlayerCRUD"),
    MONGO("MongoPlayerCRUD");

    private final String className;

    DataSource(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
