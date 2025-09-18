package com.estapar.garagem.controller;

import com.estapar.garagem.dto.PaymentResponse;
import com.estapar.garagem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista pagamentos")
    public List<PaymentResponse> listAll() {
        return service.listAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca pagamento por id")
    public PaymentResponse getById(@PathVariable("id") Long id) {
        return service.getById(id);
    }

    @PostMapping("/from-session/{sessionId}")
    @Operation(summary = "Gera pagamento a partir de uma sessão de estacionamento")
    public ResponseEntity<PaymentResponse> createFromSession(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam(name = "freeMinutesThreshold", required = false, defaultValue = "30") long freeMinutesThreshold
    ) {
        PaymentResponse created = service.createFromSession(sessionId, freeMinutesThreshold);
        return ResponseEntity.created(URI.create("/api/v1/payments/" + created.id())).body(created);
    }
}
