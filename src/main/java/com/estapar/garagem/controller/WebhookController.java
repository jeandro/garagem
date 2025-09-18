package com.estapar.garagem.controller;
import com.estapar.garagem.service.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.Map;
@RestController
@RequestMapping
@Tag(name = "Webhook")
public class WebhookController {
    private final WebhookService webhookService;
    public WebhookController(WebhookService webhookService) { this.webhookService = webhookService; }
    @PostMapping("/webhook")
    @Operation(summary = "Recebe eventos ENTRY/PARKED/EXIT")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receive(@RequestBody Map<String, Object> payload) {
        String type = String.valueOf(payload.get("event_type"));
        switch (type) {
            case "ENTRY" -> {
                String plate = String.valueOf(payload.get("license_plate"));
                String entry = String.valueOf(payload.get("entry_time"));
                webhookService.onEntry(plate, OffsetDateTime.parse(entry));
            }
            case "PARKED" -> {
                String plate = String.valueOf(payload.get("license_plate"));
                double lat = Double.parseDouble(String.valueOf(payload.get("lat")));
                double lng = Double.parseDouble(String.valueOf(payload.get("lng")));
                webhookService.onParked(plate, lat, lng);
            }
            case "EXIT" -> {
                String plate = String.valueOf(payload.get("license_plate"));
                String exit = String.valueOf(payload.get("exit_time"));
                webhookService.onExit(plate, OffsetDateTime.parse(exit));
            }
            default -> throw new IllegalArgumentException("event_type inválido: " + type);
        }
    }
}
