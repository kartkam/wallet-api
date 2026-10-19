package com.example.wallet.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.wallet.domain.Account;
import com.example.wallet.error.AccountNotFoundException;
import com.example.wallet.error.InsufficientFundsException;
import com.example.wallet.service.AccountService;

class AccountControllerTest {

    private AccountService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(AccountService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AccountController(service))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    private static Account account(long id, String owner, String currency, String balance) {
        Account account = new Account(owner, currency);
        ReflectionTestUtils.setField(account, "id", id);
        account.credit(new BigDecimal(balance));
        return account;
    }

    @Test
    void openAccountReturnsCreatedAccount() throws Exception {
        when(service.open("alice", "USD")).thenReturn(account(1L, "alice", "USD", "0"));

        mvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ownerId\":\"alice\",\"currency\":\"USD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ownerId").value("alice"))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void openAccountRejectsInvalidCurrency() throws Exception {
        mvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ownerId\":\"alice\",\"currency\":\"US\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownAccountReturns404() throws Exception {
        when(service.get(99L)).thenThrow(new AccountNotFoundException(99L));

        mvc.perform(get("/accounts/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Account 99 not found"));
    }

    @Test
    void depositRejectsNegativeAmount() throws Exception {
        mvc.perform(post("/accounts/1/deposits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transferSucceedsWithNoContent() throws Exception {
        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":1,\"toAccountId\":2,\"amount\":10.00}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void transferWithInsufficientFundsReturnsConflict() throws Exception {
        doThrow(new InsufficientFundsException(1L, new BigDecimal("5"), new BigDecimal("10")))
                .when(service).transfer(eq(1L), eq(2L), any(BigDecimal.class));

        mvc.perform(post("/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":1,\"toAccountId\":2,\"amount\":10.00}"))
                .andExpect(status().isConflict());
    }
}
