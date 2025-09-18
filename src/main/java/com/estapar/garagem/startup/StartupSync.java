package com.estapar.garagem.startup;
import com.estapar.garagem.service.GarageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
@ConditionalOnProperty(name = "app.sync.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class StartupSync implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(StartupSync.class);
    private final GarageService garageService;
    public StartupSync(GarageService garageService) { this.garageService = garageService; }
    @Override
    public void run(String... args) {
        try {
            log.info("Sincronizando garagem com simulador...");
            garageService.syncFromSimulator();
            log.info("Sincronização concluída com sucesso.");
        } catch (Exception e) {
            log.warn("Falha ao sincronizar com simulador em startup", e);
        }
    }
}
