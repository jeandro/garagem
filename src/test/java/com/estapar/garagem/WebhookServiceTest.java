package com.estapar.garagem;
import com.estapar.garagem.domain.Sector;
import com.estapar.garagem.exception.BusinessException;
import com.estapar.garagem.repository.ParkingSessionRepository;
import com.estapar.garagem.repository.SectorRepository;
import com.estapar.garagem.repository.SpotRepository;
import com.estapar.garagem.service.PricingService;
import com.estapar.garagem.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
public class WebhookServiceTest {
    private ParkingSessionRepository sessionRepo;
    private SectorRepository sectorRepo;
    private SpotRepository spotRepo;
    private WebhookService service;
    @BeforeEach
    void setup() {
        sessionRepo = mock(ParkingSessionRepository.class);
        sectorRepo = mock(SectorRepository.class);
        spotRepo = mock(SpotRepository.class);
        service = new WebhookService(sessionRepo, sectorRepo, spotRepo, new PricingService());
    }
    @Test
    void entryBlocksWhenFull() {
        Sector s = new Sector("A", new BigDecimal("10.00"), 1);
        when(sectorRepo.findAll()).thenReturn(List.of(s));
        when(spotRepo.countBySectorAndOccupiedTrue(s)).thenReturn(1L);
        assertThrows(BusinessException.class, () -> service.onEntry("AAA0A00", OffsetDateTime.now()));
    }
}
