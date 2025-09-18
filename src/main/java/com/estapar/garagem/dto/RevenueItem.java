package com.estapar.garagem.dto;
import java.math.BigDecimal;
import java.time.LocalDate;
public record RevenueItem(String sector, LocalDate date, BigDecimal total) {}
