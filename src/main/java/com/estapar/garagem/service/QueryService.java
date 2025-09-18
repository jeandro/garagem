package com.estapar.garagem.service;
import com.estapar.garagem.domain.Spot;
import com.estapar.garagem.dto.PlateStatusResponse;
import com.estapar.garagem.dto.RevenueItem;
import com.estapar.garagem.dto.SpotStatusResponse;
import com.estapar.garagem.exception.NotFoundException;
import com.estapar.garagem.repository.ParkingSessionRepository;
import com.estapar.garagem.repository.SpotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Service
public class QueryService {
    private final ParkingSessionRepository sessionRepository;
    private final SpotRepository spotRepository;
    public QueryService(ParkingSessionRepository sessionRepository, SpotRepository spotRepository) {
        this.sessionRepository = sessionRepository;
        this.spotRepository = spotRepository;
    }
    @Transactional(readOnly = true)
    public PlateStatusResponse plateStatus(String plate) {
        return sessionRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(plate)
                .map(s -> new PlateStatusResponse(plate, s.getSpot() == null ? "ENTERED" : "PARKED"))
                .orElseGet(() -> new PlateStatusResponse(plate, "NOT_FOUND"));
    }
    @Transactional(readOnly = true)
    public SpotStatusResponse spotStatus(Long id) {
        Spot spot = spotRepository.findById(id).orElseThrow(() -> new NotFoundException("Vaga " + id + " não existe"));
        return new SpotStatusResponse(spot.getId(), spot.getSector().getName(), spot.isOccupied(), spot.getCurrentPlate());
    }
    @Transactional(readOnly = true)
    public List<RevenueItem> revenue(LocalDate from, LocalDate to) {
        return sessionRepository.aggregateRevenue(from, to).stream()
                .map(arr -> new RevenueItem((String) arr[0], (LocalDate) arr[1], (BigDecimal) arr[2]))
                .toList();
    }
}
