package model;

import Interface.VerifiedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author SIN
 */
@javax.persistence.Entity
@Entity
@Table(name = "region")
public class Region implements Serializable, VerifiedEntity {
    @javax.persistence.Id
    @Id
    @Column(name = "name_region")
    private String name;
    private static final long SERIAL_VERSION = 1L;

    public Region() {

    }

    public Region(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Region region = (Region) object;
        return Objects.equals(name, region.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int getID() {
        return 0;
    }
}
