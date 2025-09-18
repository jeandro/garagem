package com.estapar.garagem.service;
import com.estapar.garagem.domain.Sector;
import com.estapar.garagem.domain.Spot;
import com.estapar.garagem.repository.SectorRepository;
import com.estapar.garagem.repository.SpotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
@Service
public class GarageService {
    private static final Logger log = LoggerFactory.getLogger(GarageService.class);
    private final SectorRepository sectorRepository;
    private final SpotRepository spotRepository;
    private final RestClient restClient;
    private final String baseUrl;
    public GarageService(SectorRepository sectorRepository, SpotRepository spotRepository,
                         @Value("${app.simulator-base-url}") String baseUrl) {
        this.sectorRepository = sectorRepository;
        this.spotRepository = spotRepository;
        this.baseUrl = baseUrl;
        this.restClient = RestClient.create();
    }
    @SuppressWarnings("unchecked")
    public void syncFromSimulator() {
        ResponseEntity<Map> resp = restClient.get().uri(baseUrl + "/garage").retrieve().toEntity(Map.class);
        Map<String, Object> body = resp.getBody();
        if (body == null) { log.warn("Resposta vazia do simulador {}", baseUrl); return; }
        List<Map<String, Object>> garage = (List<Map<String, Object>>) body.get("garage");
        List<Map<String, Object>> spots = (List<Map<String, Object>>) body.get("spots");
        if (garage == null || spots == null) {
            log.warn("Estrutura inesperada no payload: chaves 'garage' e/ou 'spots' ausentes.");
            return;
        }
        for (Map<String, Object> g : garage) {
            String sectorName = readString(g, "sector", "setor");
            BigDecimal basePrice = readBigDecimal(g, new String[]{"basePrice","base_price","price"}, new BigDecimal("10.00"));
            Integer maxCap = readInt(g, new String[]{"max_capacity","maxCapacity","capacity","maxSpots"}, 0);
            if (sectorName == null || sectorName.isBlank()) { log.warn("Setor inválido: {}", g); continue; }
            Sector sector = sectorRepository.findByName(sectorName).orElse(new Sector(sectorName, basePrice, maxCap));
            sector.setBasePrice(basePrice);
            sector.setMaxCapacity(maxCap);
            sectorRepository.save(sector);
        }
        for (Map<String, Object> s : spots) {
            String sectorName = readString(s, "sector", "setor");
            Sector sector = sectorRepository.findByName(sectorName).orElse(null);
            if (sector == null) { log.warn("Setor '{}' não encontrado ao criar spot. Payload: {}", sectorName, s); continue; }
            double lat = readDouble(s, new String[]{"lat","latitude","y"}, 0);
            double lng = readDouble(s, new String[]{"lng","lon","longitude","x"}, 0);
            spotRepository.save(new Spot(sector, lat, lng));
        }
    }
    private String readString(Map<String, Object> map, String... keys) {
        for (String k : keys) { Object v = map.get(k); if (v != null) return String.valueOf(v); }
        return null;
    }
    private Integer readInt(Map<String, Object> map, String[] keys, int def) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v == null) continue;
            if (v instanceof Number n) return n.intValue();
            try { return Integer.parseInt(String.valueOf(v)); } catch (Exception ignored) {}
        }
        return def;
    }
    private double readDouble(Map<String, Object> map, String[] keys, double def) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v == null) continue;
            if (v instanceof Number n) return n.doubleValue();
            try { return Double.parseDouble(String.valueOf(v)); } catch (Exception ignored) {}
        }
        return def;
    }
    private BigDecimal readBigDecimal(Map<String, Object> map, String[] keys, BigDecimal def) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v == null) continue;
            if (v instanceof Number n) return new BigDecimal(n.toString()).setScale(2, RoundingMode.HALF_UP);
            String s = String.valueOf(v).trim();
            if (s.equalsIgnoreCase("null") || s.isEmpty()) continue;
            try { return new BigDecimal(s).setScale(2, RoundingMode.HALF_UP); } catch (Exception ignored) {}
        }
        return def;
    }
}
