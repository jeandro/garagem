package com.estapar.garagem.service;
import com.estapar.garagem.domain.ParkingSession;
import com.estapar.garagem.domain.Sector;
import com.estapar.garagem.domain.Spot;
import com.estapar.garagem.exception.BusinessException;
import com.estapar.garagem.exception.NotFoundException;
import com.estapar.garagem.repository.ParkingSessionRepository;
import com.estapar.garagem.repository.SectorRepository;
import com.estapar.garagem.repository.SpotRepository;
import com.estapar.garagem.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class WebhookService {
	private static final int FREE_MINUTES = 30;

	private final ParkingSessionRepository sessionRepository;
	private final SectorRepository sectorRepository;
	private final SpotRepository spotRepository;
	private final PricingService pricingService;
	private final PaymentRepository paymentRepository;
	@Autowired
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

	// ... existing code ...
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
	// ... existing code ...
	@Transactional
	public void onEntry(String plate, OffsetDateTime entryTime) {
		String normalizedPlate = normalizePlate(plate);
		if (entryTime == null) {
			throw new BusinessException("Data/hora de entrada inválida");
		}
		// Impede múltiplas sessões abertas para a mesma placa
		sessionRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(normalizedPlate)
				.ifPresent(s -> { throw new BusinessException("Já existe sessão aberta para a placa " + normalizedPlate); });

		Sector chosen = sectorRepository.findAll().stream()
				.filter(s -> s.getMaxCapacity() != null && s.getMaxCapacity() > 0)
				.min(Comparator.comparingDouble(this::occupancyRatio))
				.orElseThrow(() -> new NotFoundException("Nenhum setor configurado"));

		long occupied = spotRepository.countBySectorAndOccupiedTrue(chosen);
		long total = chosen.getMaxCapacity();
		if (occupied >= total) throw new BusinessException("Setor " + chosen.getName() + " está lotado");

		BigDecimal price = pricingService.priceAtEntry(chosen, occupied, total);
		if (price == null) price = chosen.getBasePrice();

		ParkingSession ps = new ParkingSession(normalizedPlate);
		ps.setEntryTime(entryTime);
		ps.setSector(chosen);
		ps.setAppliedPrice(price);
		sessionRepository.save(ps);
		// Pagamento será gerado no EXIT
	}
	// ... existing code ...
	@Transactional
	public void onParked(String plate, double lat, double lng) {
		String normalizedPlate = normalizePlate(plate);
		ParkingSession ps = sessionRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(normalizedPlate)
				.orElseThrow(() -> new NotFoundException("Sessão não encontrada para " + normalizedPlate));

		List<Spot> free = spotRepository.findBySectorAndOccupiedFalse(ps.getSector());
		if (free.isEmpty()) throw new BusinessException("Sem vagas livres no setor " + ps.getSector().getName());

		// Seleção determinística da vaga (menor id)
		Spot spot = free.stream()
				.min(Comparator.comparing(Spot::getId))
				.orElse(free.get(0));

		spot.setOccupied(true);
		spot.setCurrentPlate(normalizedPlate);
		spotRepository.save(spot);

		ps.setSpot(spot);
		ps.setParkedTime(OffsetDateTime.now());
		sessionRepository.save(ps);
		// Pagamento não é criado aqui para evitar duplicidade
	}
	// ... existing code ...
	@Transactional
	public void onExit(String plate, OffsetDateTime exitTime) {
		String normalizedPlate = normalizePlate(plate);
		if (exitTime == null) {
			throw new BusinessException("Data/hora de saída inválida");
		}
		ParkingSession ps = sessionRepository.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(normalizedPlate)
				.orElseThrow(() -> new NotFoundException("Sessão não encontrada para " + normalizedPlate));

		if (ps.getEntryTime() != null && exitTime.isBefore(ps.getEntryTime())) {
			throw new BusinessException("Horário de saída anterior à entrada");
		}

		ps.setExitTime(exitTime);
		if (ps.getSpot() != null) {
			Spot spot = ps.getSpot();
			spot.setOccupied(false);
			spot.setCurrentPlate(null);
			spotRepository.save(spot);
		}

		BigDecimal applied = ps.getAppliedPrice() != null ? ps.getAppliedPrice()
				: (ps.getSector() != null ? ps.getSector().getBasePrice() : BigDecimal.ZERO);
		BigDecimal total = calculatePrice(ps.getEntryTime(), exitTime, applied);
		ps.setTotalPrice(total);
		sessionRepository.save(ps);

		// Gera/salva pagamento apenas no EXIT
		com.estapar.garagem.domain.Payment payment = com.estapar.garagem.domain.Payment.createFromParkSession(ps, FREE_MINUTES);
		if (paymentRepository != null) {
			paymentRepository.save(payment);
		}
	}
	// ... existing code ...
	private BigDecimal calculatePrice(OffsetDateTime entry, OffsetDateTime exit, BigDecimal pricePerHour) {
		if (entry == null || exit == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		long minutes = Duration.between(entry, exit).toMinutes();
		if (minutes <= 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

		long billableMinutes = Math.max(0, minutes - FREE_MINUTES);
		long billableHours = (billableMinutes + 59) / 60; // arredonda para cima sem double
		BigDecimal total = pricePerHour.multiply(BigDecimal.valueOf(billableHours));
		return total.setScale(2, RoundingMode.HALF_UP);
	}
	// ... existing code ...
	private double occupancyRatio(Sector s) {
		long occ = spotRepository.countBySectorAndOccupiedTrue(s);
		return s.getMaxCapacity() == 0 ? 0.0 : ((double) occ / (double) s.getMaxCapacity());
	}

	private String normalizePlate(String plate) {
		return plate == null ? null : plate.trim().toUpperCase();
	}
}