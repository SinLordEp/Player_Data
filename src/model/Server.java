package model;

import Interface.VerifiedEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author SIN
 */
@javax.persistence.Entity
@Entity
@Table(name = "server")
public class Server implements Serializable,VerifiedEntity {
    @javax.persistence.Id
    @Id
    @Column(name = "name_server")
    private String name;
    private static final long SERIAL_VERSION = 1L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region", referencedColumnName = "name_region")
    private Region region;

    public Server() {
    }

    public Server(String name, Region region) {
        this.name = name;
        this.region = region;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Server server = (Server) object;
        return Objects.equals(name, server.name);
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
