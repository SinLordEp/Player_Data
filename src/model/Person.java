package model;

public class Person implements java.io.Serializable{
    private int ID = 0;
    private String name;
    private String server;
    private String region;
    private static final long serialVersion = 1L;

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return this.server;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getID() {
        return this.ID;
    }

    public String getName() {
        return this.name;
    }

    public String getRegion() {
        return this.region;
    }

    @Override
    public String toString() {
        return """
                Region: %s
                Server: %s
                ID: %s
                Name: %s""".formatted(region, server, ID, name);
    }

    public boolean isValid(){
        return ID == 0 && name.isBlank() && region.isBlank() && server.isBlank();
    }
}
