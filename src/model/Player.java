package model;

public class Player implements Person,java.io.Serializable {
    private int ID;
    private String name;
    private String server;
    private String region;
    private static final long serialVersion = 1L;

    public Player() {
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return this.server;
    }

    @Override
    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public void setServer(String server) {
        this.server = server;
    }

    @Override
    public int getID() {
        return this.ID;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getRegion() {
        return this.region;
    }

    @Override
    public String toString() {
        return "Person type: Player"
                +"\nRegion: " + this.region
                +"\nServer: " + this.server
                +"\nID: " + this.ID
                +"\nName: " + this.name;
    }

}
