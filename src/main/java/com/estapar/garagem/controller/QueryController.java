package com.estapar.garagem.controller;
import com.estapar.garagem.dto.PlateStatusResponse;
import com.estapar.garagem.dto.RevenueItem;
import com.estapar.garagem.dto.SpotStatusResponse;
import com.estapar.garagem.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping
@Tag(name = "Queries")
public class QueryController {

	private final QueryService queryService;

	public QueryController(QueryService queryService) {
		this.queryService = queryService;
	}

	@GetMapping("/plate-status")
	@Operation(summary = "Status de uma placa (NOT_FOUND|ENTERED|PARKED)")
	public PlateStatusResponse plateStatus(@RequestParam("plate") String plate) {
		return queryService.plateStatus(plate);
	}

	@GetMapping("/spot-status")
	@Operation(summary = "Status de uma vaga")
	public SpotStatusResponse spotStatus(@RequestParam("id") Long id) {
		return queryService.spotStatus(id);
	}

	@GetMapping("/revenue")
	@Operation(summary = "Receita total por setor e data")
	public List<RevenueItem> revenue(
			@RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return queryService.revenue(from, to);
	}
}