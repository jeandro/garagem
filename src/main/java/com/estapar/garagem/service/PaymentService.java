package com.estapar.garagem.service;

import com.estapar.garagem.domain.Payment;
import com.estapar.garagem.domain.ParkingSession;
import com.estapar.garagem.dto.PaymentResponse;
import com.estapar.garagem.exception.NotFoundException;
import com.estapar.garagem.repository.PaymentRepository;
import com.estapar.garagem.repository.ParkingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ParkingSessionRepository sessionRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          ParkingSessionRepository sessionRepository) {
        this.paymentRepository = paymentRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> listAll() {
        return paymentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PaymentResponse getById(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment " + id + " não encontrado"));
        return toResponse(p);
    }

    @Transactional
    public PaymentResponse createFromSession(Long sessionId, long freeMinutesThreshold) {
        ParkingSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("ParkingSession " + sessionId + " não encontrada"));
        Payment payment = Payment.createFromParkSession(session, freeMinutesThreshold);
        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getParkSession().getId(),
                p.getSector() != null ? p.getSector().getName() : null,
                p.getSpot() != null ? p.getSpot().getId() : null,
                p.getEntryTime(),
                p.getExitTime(),
                p.getSpentTimeMinutes(),
                p.getSectorPrice(),
                p.getTotalPrice()
        );
    }
}
