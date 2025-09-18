package com.estapar.garagem.domain;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name = "sectors")
public class Sector {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 10)
    private String name;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    @Column(nullable = false)
    private Integer maxCapacity;
    protected Sector() {}
    public Sector(String name, BigDecimal basePrice, Integer maxCapacity) {
        this.name = name;
        this.basePrice = basePrice;
        this.maxCapacity = maxCapacity;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getBasePrice() { return basePrice; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setName(String name) { this.name = name; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
}
