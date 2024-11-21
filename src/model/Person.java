package model;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Person implements java.io.Serializable{
    @Id
    @Column(name = "id")
    private int ID = 0;

    @Column(name = "name")
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
