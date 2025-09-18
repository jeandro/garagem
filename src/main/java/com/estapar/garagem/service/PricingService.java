package com.estapar.garagem.service;
import com.estapar.garagem.domain.Sector;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
@Service
public class PricingService {
    public BigDecimal priceAtEntry(Sector sector, long occupied, long totalSpots) {
        if (totalSpots == 0) return sector.getBasePrice();
        double ratio = (double) occupied / (double) totalSpots;
        BigDecimal base = sector.getBasePrice();
        BigDecimal factor;
        if (ratio < 0.25) factor = new BigDecimal("0.90");
        else if (ratio <= 0.50) factor = BigDecimal.ONE;
        else if (ratio <= 0.75) factor = new BigDecimal("1.10");
        else factor = new BigDecimal("1.25");
        return base.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
