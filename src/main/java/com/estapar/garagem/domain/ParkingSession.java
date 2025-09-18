package com.estapar.garagem.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Entity
@Table(name = "parking_sessions", indexes = {
        @Index(name = "idx_session_plate_open", columnList = "licensePlate, exitTime")
})
public class ParkingSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 12)
    private String licensePlate;
    @ManyToOne private Sector sector;
    @ManyToOne private Spot spot;
    private OffsetDateTime entryTime;
    private OffsetDateTime parkedTime;
    private OffsetDateTime exitTime;
    @Column(precision = 10, scale = 2)
    private BigDecimal appliedPrice;
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;
    protected ParkingSession() {}
    public ParkingSession(String licensePlate) { this.licensePlate = licensePlate; }
    public Long getId() { return id; }
    public String getLicensePlate() { return licensePlate; }
    public Sector getSector() { return sector; }
    public Spot getSpot() { return spot; }
    public OffsetDateTime getEntryTime() { return entryTime; }
    public OffsetDateTime getParkedTime() { return parkedTime; }
    public OffsetDateTime getExitTime() { return exitTime; }
    public BigDecimal getAppliedPrice() { return appliedPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setSector(Sector sector) { this.sector = sector; }
    public void setSpot(Spot spot) { this.spot = spot; }
    public void setEntryTime(OffsetDateTime entryTime) { this.entryTime = entryTime; }
    public void setParkedTime(OffsetDateTime parkedTime) { this.parkedTime = parkedTime; }
    public void setExitTime(OffsetDateTime exitTime) { this.exitTime = exitTime; }
    public void setAppliedPrice(BigDecimal appliedPrice) { this.appliedPrice = appliedPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
