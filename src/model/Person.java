package model;

public interface Person {
    int getID();
    String getName();
    String getRegion();
    String getServer();
    void setRegion(String region);
    void setServer(String server);
    void setID(int ID);
    void setName(String name);
}
