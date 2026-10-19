package com.example.wallet.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.service.AccountQueryService;
import com.example.wallet.service.AccountSummary;

class AccountListControllerTest {

    private AccountQueryService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(AccountQueryService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AccountListController(service)).build();
    }

    @Test
    void listsAccountsWithRecentActivity() throws Exception {
        Account account = new Account("alice", "USD");
        ReflectionTestUtils.setField(account, "id", 7L);
        account.credit(new BigDecimal("12.00"));
        LedgerEntry entry = new LedgerEntry(7L, EntryType.DEPOSIT, new BigDecimal("12.00"), null,
                Instant.parse("2026-01-01T10:00:00Z"));
        when(service.list(1, 2)).thenReturn(List.of(new AccountSummary(account, List.of(entry))));

        mvc.perform(get("/accounts").param("page", "1").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].recentActivity[0].type").value("DEPOSIT"));
    }

    @Test
    void usesDefaultPagingWhenNoneIsGiven() throws Exception {
        when(service.list(1, 20)).thenReturn(List.of());

        mvc.perform(get("/accounts")).andExpect(status().isOk());

        verify(service).list(1, 20);
    }
}
