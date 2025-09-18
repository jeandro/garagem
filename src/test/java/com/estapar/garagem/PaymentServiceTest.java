package com.estapar.garagem;

import com.estapar.garagem.domain.ParkingSession;
import com.estapar.garagem.domain.Sector;
import com.estapar.garagem.domain.Spot;
import com.estapar.garagem.dto.PaymentResponse;
import com.estapar.garagem.repository.ParkingSessionRepository;
import com.estapar.garagem.repository.PaymentRepository;
import com.estapar.garagem.service.PaymentService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PaymentServiceTest {

    @Test
    void createFromSession_respeita30MinGratis() {
        ParkingSessionRepository sessionRepo = mock(ParkingSessionRepository.class);
        PaymentRepository paymentRepo = mock(PaymentRepository.class);

        Sector sector = new Sector("A", new BigDecimal("10.00"), 100);
        Spot spot = new Spot(sector, 0, 0);

        ParkingSession session = new ParkingSession("ABC1D23");
        session.setSector(sector);
        session.setSpot(spot);
        session.setAppliedPrice(new BigDecimal("10.00"));
        session.setEntryTime(OffsetDateTime.now().minusMinutes(89));
        session.setExitTime(OffsetDateTime.now());

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(paymentRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PaymentService svc = new PaymentService(paymentRepo, sessionRepo);
        PaymentResponse resp = svc.createFromSession(1L, 30);

        assertEquals(new BigDecimal("10.00"), resp.totalPrice());
    }
}
