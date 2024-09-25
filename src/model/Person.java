package model;

public class Person implements java.io.Serializable{
    private int ID = 0;
    private String name;

    private static final long serialVersion = 1L;

    public void setID(int ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return this.ID;
    }

    public String getName() {
        return this.name;
    }



}
