package com.estapar.garagem.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long parkSessionId,
        String sector,
        Long spotId,
        OffsetDateTime entryTime,
        OffsetDateTime exitTime,
        Long spentTimeMinutes,
        BigDecimal sectorPrice,
        BigDecimal totalPrice
) {}
