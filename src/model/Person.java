package model;

import Interface.VerifiedEntity;
import jakarta.persistence.*;

/**
 * @author SIN
 */
@javax.persistence.Entity
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Person implements java.io.Serializable, VerifiedEntity {
    @javax.persistence.Id
    @javax.persistence.GeneratedValue
    @Id
    @GeneratedValue
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
