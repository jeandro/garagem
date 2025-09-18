package com.estapar.garagem.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_park_session", columnList = "park_session_id"),
        @Index(name = "idx_payments_sector", columnList = "sector_id")
    }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "park_session_id", nullable = false)
    private ParkingSession parkSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private Spot spot;

    @Column(name = "entry_time", nullable = false)
    private OffsetDateTime entryTime;

    @Column(name = "exit_time", nullable = false)
    private OffsetDateTime exitTime;

    @Column(name = "spent_time_minutes", nullable = false)
    private Long spentTimeMinutes;

    @Column(name = "sector_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sectorPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    protected Payment() {}

    public Payment(ParkingSession parkSession,
                   Sector sector,
                   Spot spot,
                   OffsetDateTime entryTime,
                   OffsetDateTime exitTime,
                   Long spentTimeMinutes,
                   BigDecimal sectorPrice,
                   BigDecimal totalPrice) {
        this.parkSession = parkSession;
        this.sector = sector;
        this.spot = spot;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.spentTimeMinutes = spentTimeMinutes;
        this.sectorPrice = sectorPrice;
        this.totalPrice = totalPrice;
    }

    public static Payment createFromParkSession(ParkingSession session, long freeMinutesThreshold) {
        OffsetDateTime exit = session.getExitTime() != null ? session.getExitTime() : OffsetDateTime.now();
        long spentMin = java.time.Duration.between(session.getEntryTime(), exit).toMinutes();
        java.math.BigDecimal pricePerHour = session.getAppliedPrice();
        if (pricePerHour == null) {
            pricePerHour = (session.getSpot() != null && session.getSpot().getSector() != null)
                    ? session.getSpot().getSector().getBasePrice()
                    : java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal total = calculateTotal(pricePerHour, spentMin, freeMinutesThreshold);
        Sector sector = session.getSpot() != null ? session.getSpot().getSector() : null;
        return new Payment(
                session, sector, session.getSpot(), session.getEntryTime(), exit, spentMin,
                pricePerHour.setScale(2, java.math.RoundingMode.HALF_UP),
                total
        );
    }

    private static java.math.BigDecimal calculateTotal(java.math.BigDecimal pricePerHour, long spentMin, long freeThreshold) {
        if (spentMin <= freeThreshold) return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        long billableMinutes = spentMin - freeThreshold;
        long hours = (long) Math.ceil(billableMinutes / 60.0);
        return pricePerHour.multiply(java.math.BigDecimal.valueOf(hours)).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public ParkingSession getParkSession() { return parkSession; }
    public Sector getSector() { return sector; }
    public Spot getSpot() { return spot; }
    public OffsetDateTime getEntryTime() { return entryTime; }
    public OffsetDateTime getExitTime() { return exitTime; }
    public Long getSpentTimeMinutes() { return spentTimeMinutes; }
    public BigDecimal getSectorPrice() { return sectorPrice; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
