package com.estapar.garagem;
import com.estapar.garagem.controller.QueryController;
import com.estapar.garagem.dto.PlateStatusResponse;
import com.estapar.garagem.dto.RevenueItem;
import com.estapar.garagem.service.QueryService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(QueryController.class)
public class QueryControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private QueryService service;
    @Test
    void plateStatus_ok() throws Exception {
        Mockito.when(service.plateStatus("ABC1D23")).thenReturn(new PlateStatusResponse("ABC1D23", "PARKED"));
        mvc.perform(get("/plate-status").param("plate", "ABC1D23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PARKED")));
    }
    @Test
    void revenue_ok() throws Exception {
        Mockito.when(service.revenue(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-31")))
                .thenReturn(List.of(new RevenueItem("A", LocalDate.parse("2025-01-10"), new BigDecimal("20.00"))));
        mvc.perform(get("/revenue").param("from", "2025-01-01").param("to", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sector", is("A")));
    }
}
