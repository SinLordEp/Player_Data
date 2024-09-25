package model;

public class Player extends Person {
    private String server;
    private String region;

    public Player() {
        super();
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

    public String getRegion() {
        return this.region;
    }

    @Override
    public String toString() {
        return """
                Region: %s
                Server: %s
                ID: %s
                Name: %s""".formatted(region, server, super.getID(), super.getName());
    }


}
