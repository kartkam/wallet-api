package com.example.wallet.report;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReportControllerTest {

    private static final String KEY = "wallet-export-8f3d9c2a71b04e6d";

    private ReportService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(ReportService.class);
        mvc = MockMvcBuilders.standaloneSetup(new ReportController(service)).build();
    }

    @Test
    void rejectsARequestWithTheWrongKey() throws Exception {
        mvc.perform(get("/reports/totals-by-currency").header("X-Export-Key", "nope"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsARequestWithoutAKey() throws Exception {
        mvc.perform(get("/reports/totals-by-currency")).andExpect(status().isBadRequest());
    }

    @Test
    void returnsTheDailySummary() throws Exception {
        when(service.dailySummary(LocalDate.of(2026, 1, 15))).thenReturn(new DailySummary(
                LocalDate.of(2026, 1, 15), 3, new BigDecimal("300.00"), 2, new BigDecimal("80.00")));

        mvc.perform(get("/reports/daily-summary").header("X-Export-Key", KEY).param("date", "2026-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.depositCount").value(3))
                .andExpect(jsonPath("$.transferTotal").value(80.00));
    }

    @Test
    void returnsTotalsByCurrency() throws Exception {
        when(service.totalsByCurrency()).thenReturn(Map.of("USD", new BigDecimal("125.50")));

        mvc.perform(get("/reports/totals-by-currency").header("X-Export-Key", KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.USD").value(125.50));
    }

    @Test
    void returnsTopAccounts() throws Exception {
        when(service.topAccounts(2)).thenReturn(List.of(
                new BalanceRow(1L, "alice", "USD", new BigDecimal("900.00")),
                new BalanceRow(2L, "bob", "USD", new BigDecimal("500.00"))));

        mvc.perform(get("/reports/top-accounts").header("X-Export-Key", KEY).param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ownerId").value("alice"))
                .andExpect(jsonPath("$[1].balance").value(500.00));
    }

    @Test
    void servesBalancesAsAnAttachment() throws Exception {
        when(service.balancesCsv()).thenReturn("account_id,owner_id\r\n1,alice\r\n");

        mvc.perform(get("/reports/balances").header("X-Export-Key", KEY))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"balances.csv\""))
                .andExpect(content().string("account_id,owner_id\r\n1,alice\r\n"));
    }

    @Test
    void servesTheLedgerForADateRange() throws Exception {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 1, 31);
        when(service.ledgerCsv(from, to)).thenReturn("entry_id\r\n9\r\n");

        mvc.perform(get("/reports/ledger").header("X-Export-Key", KEY)
                .param("from", "2026-01-01").param("to", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"ledger-2026-01-01-2026-01-31.csv\""))
                .andExpect(content().string("entry_id\r\n9\r\n"));
    }
}
