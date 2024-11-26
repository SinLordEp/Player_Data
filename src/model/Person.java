package model;

import jakarta.persistence.*;

/**
 * @author SIN
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Person implements java.io.Serializable{
    @Id
    @Column(name = "id")
    protected int ID = 0;

    @Column(name = "name")
    protected String name;

    private static final long SERIAL_VERSION = 1L;

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
