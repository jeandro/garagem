package com.estapar.garagem;
import com.estapar.garagem.domain.Sector;
import com.estapar.garagem.service.PricingService;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
public class PricingServiceTest {
    @Test
    void pricingBrackets() {
        PricingService svc = new PricingService();
        Sector s = new Sector("A", new BigDecimal("10.00"), 100);
        assertEquals(new BigDecimal("9.00"), svc.priceAtEntry(s, 0, 100));
        assertEquals(new BigDecimal("10.00"), svc.priceAtEntry(s, 25, 100));
        assertEquals(new BigDecimal("11.00"), svc.priceAtEntry(s, 51, 100));
        assertEquals(new BigDecimal("12.50"), svc.priceAtEntry(s, 99, 100));
    }
}
