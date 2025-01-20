package data;

/**
 * @author SIN
 */

public enum DataSource {
    NONE, FILE, DATABASE, HIBERNATE, PHP, OBJECTDB;

    public static DataSource fromString(String input) {
        for (DataSource dataSource : DataSource.values()) {
            if (dataSource.toString().equalsIgnoreCase(input)) {
                return dataSource;
            }
        }
        throw new IllegalArgumentException("No enum constant matches the input: " + input);
    }
}
