package model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * @author SIN
 */
@Entity
@Table(name = "region")
public class Region {
    @Id
    @Column(name = "name_region")
    private String name;

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
}
