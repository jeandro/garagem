package com.estapar.garagem.domain;
import jakarta.persistence.*;
@Entity
@Table(name = "spots")
public class Spot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Sector sector;
    private double lat;
    private double lng;
    private boolean occupied;
    private String currentPlate;
    protected Spot() {}
    public Spot(Sector sector, double lat, double lng) {
        this.sector = sector;
        this.lat = lat;
        this.lng = lng;
        this.occupied = false;
    }
    public Long getId() { return id; }
    public Sector getSector() { return sector; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public boolean isOccupied() { return occupied; }
    public String getCurrentPlate() { return currentPlate; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }
    public void setCurrentPlate(String currentPlate) { this.currentPlate = currentPlate; }
}
