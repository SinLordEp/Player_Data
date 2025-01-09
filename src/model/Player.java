package model;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * @author SIN
 */
@javax.persistence.Entity
@Entity
@Table(name = "player")
public class Player extends Person {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server", referencedColumnName = "name_server")
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region", referencedColumnName = "name_region")
    private Region region;

    public Player() {
        super();
    }

    public Server getServer() {
        return this.server;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Region getRegion() {
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Player player = (Player) object;
        return Objects.equals(ID, player.ID);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ID);
    }

}
