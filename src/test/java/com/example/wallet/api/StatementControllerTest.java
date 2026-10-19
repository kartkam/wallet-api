package com.example.wallet.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.error.AccountNotFoundException;
import com.example.wallet.service.StatementService;

class StatementControllerTest {

    private StatementService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(StatementService.class);
        mvc = MockMvcBuilders.standaloneSetup(new StatementController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsStatementEntries() throws Exception {
        LedgerEntry entry = new LedgerEntry(1L, EntryType.DEPOSIT, new BigDecimal("25.00"), null,
                Instant.parse("2026-01-01T10:00:00Z"));
        when(service.statement(1L, "DEPOSIT", 10)).thenReturn(List.of(entry));

        mvc.perform(get("/accounts/1/statement")
                .header("X-Owner-Id", "alice")
                .param("type", "DEPOSIT")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(25.00));
    }

    @Test
    void missingOwnerHeaderIsABadRequest() throws Exception {
        mvc.perform(get("/accounts/1/statement")).andExpect(status().isBadRequest());
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        when(service.statement(99L, null, null)).thenThrow(new AccountNotFoundException(99L));

        mvc.perform(get("/accounts/99/statement").header("X-Owner-Id", "alice"))
                .andExpect(status().isNotFound());
    }
}
