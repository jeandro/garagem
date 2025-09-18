import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.estapar.garagem.domain.ParkingSession;
import com.estapar.garagem.domain.Sector;
import com.estapar.garagem.domain.Spot;
import com.estapar.garagem.exception.BusinessException;
import com.estapar.garagem.exception.NotFoundException;
import com.estapar.garagem.repository.ParkingSessionRepository;
import com.estapar.garagem.repository.PaymentRepository;
import com.estapar.garagem.repository.SectorRepository;
import com.estapar.garagem.repository.SpotRepository;
import com.estapar.garagem.service.PricingService;

import jakarta.transaction.Transactional;

@Service
public class WebhookService {
    private final ParkingSessionRepository sessionRepository;
    private final SectorRepository sectorRepository;
    private final SpotRepository spotRepository;
    private final PricingService pricingService;
    private final PaymentRepository paymentRepository;
    public WebhookService(ParkingSessionRepository sessionRepository,
                          SectorRepository sectorRepository,
                          SpotRepository spotRepository,
                          PricingService pricingService,
                          PaymentRepository paymentRepository) {
        this.sessionRepository = sessionRepository;
        this.sectorRepository = sectorRepository;
        this.spotRepository = spotRepository;
        this.pricingService = pricingService;
        this.paymentRepository = paymentRepository;
    }
    // Construtor usado em testes (sem PaymentRepository)
    public WebhookService(final ParkingSessionRepository sessionRepo,
                          final SectorRepository sectorRepo,
                          final SpotRepository spotRepo,
                          final PricingService pricingService) {
        this.sessionRepository = sessionRepo;
        this.sectorRepository = sectorRepo;
        this.spotRepository = spotRepo;
        this.pricingService = pricingService;
        this.paymentRepository = null; // não usado em cenários de teste que não salvam pagamento
    }
    @Transactional
    public void onEntry(String plate, OffsetDateTime entryTime) {
        Sector chosen = sectorRepository.findAll().stream()
                .min(Comparator.comparingDouble(this::occupancyRatio))
                .orElseThrow(() -> new NotFoundException("Nenhum setor configurado"));
        long occupied = spotRepository.countBySectorAndOccupiedTrue(chosen);
        long total = chosen.getMaxCapacity();
        if (occupied >= total) throw new BusinessException("Setor " + chosen.getName() + " está lotado");
        BigDecimal price = pricingService.priceAtEntry(chosen, occupied, total);
        ParkingSession ps = new ParkingSession(plate);
        ps.setEntryTime(entryTime);
        ps.setSector(chosen);
        ps.setAppliedPrice(price);
        sessionRepository.save(ps);
        com.estapar.garagem.domain.Payment payment = com.estapar.garagem.domain.Payment.createFromParkSession(ps, 30);
        if (paymentRepository != null) {
            paymentRepository.save(payment);
        }
    }
    @Transactional
    public void onParked(String plate, double lat, double lng) {
        ParkingSession ps = sessionRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(plate)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada para " + plate));
        List<Spot> free = spotRepository.findBySectorAndOccupiedFalse(ps.getSector());
        if (free.isEmpty()) throw new BusinessException("Sem vagas livres no setor " + ps.getSector().getName());
        Spot spot = free.get(0);
        spot.setOccupied(true);
        spot.setCurrentPlate(plate);
        spotRepository.save(spot);
        ps.setSpot(spot);
        ps.setParkedTime(OffsetDateTime.now());
        sessionRepository.save(ps);
        com.estapar.garagem.domain.Payment payment = com.estapar.garagem.domain.Payment.createFromParkSession(ps, 30);
        if (paymentRepository != null) {
            paymentRepository.save(payment);
        }
    }
    @Transactional
    public void onExit(String plate, OffsetDateTime exitTime) {
        ParkingSession ps = sessionRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(plate)
                .orElseThrow(() -> new NotFoundException("Sessão não encontrada para " + plate));
        ps.setExitTime(exitTime);
        if (ps.getSpot() != null) {
            Spot spot = ps.getSpot();
            spot.setOccupied(false);
            spot.setCurrentPlate(null);
            spotRepository.save(spot);
        }
        BigDecimal total = calculatePrice(ps.getEntryTime(), exitTime, ps.getAppliedPrice());
        ps.setTotalPrice(total);
        sessionRepository.save(ps);
        com.estapar.garagem.domain.Payment payment = com.estapar.garagem.domain.Payment.createFromParkSession(ps, 30);
        if (paymentRepository != null) {
            paymentRepository.save(payment);
        }
    }
    private BigDecimal calculatePrice(OffsetDateTime entry, OffsetDateTime exit, BigDecimal pricePerHour) {
        long minutes = Duration.between(entry, exit).toMinutes();
        if (minutes <= 30) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        double hours = (minutes - 30) / 60.0;
        long billable = (long) Math.ceil(hours);
        return pricePerHour.multiply(BigDecimal.valueOf(billable)).setScale(2, RoundingMode.HALF_UP);
    }
    private double occupancyRatio(Sector s) {
        long occ = spotRepository.countBySectorAndOccupiedTrue(s);
        return s.getMaxCapacity() == 0 ? 0.0 : ((double) occ / (double) s.getMaxCapacity());
    }
}
