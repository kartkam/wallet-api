package com.example.wallet.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.domain.Account;
import com.example.wallet.error.AccountNotFoundException;
import com.example.wallet.error.InsufficientFundsException;
import com.example.wallet.error.InvalidRequestException;

class ApiExceptionHandlerTest {

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new FailingController())
            .setControllerAdvice(new ApiExceptionHandler())
            .build();

    @Test
    void unknownAccountMapsToNotFound() throws Exception {
        mvc.perform(get("/fail/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Account 5 not found"));
    }

    @Test
    void insufficientFundsMapsToConflict() throws Exception {
        mvc.perform(get("/fail/insufficient-funds"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(
                        "Insufficient funds in account 5: balance 1, requested 2"));
    }

    @Test
    void invalidRequestMapsToBadRequest() throws Exception {
        mvc.perform(get("/fail/invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Amount must be positive"));
    }

    @Test
    void optimisticLockFailureMapsToConflictWithRetryHint() throws Exception {
        mvc.perform(get("/fail/concurrent-update"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value(ApiExceptionHandler.CONCURRENT_UPDATE_DETAIL));
    }

    @RestController
    public static class FailingController {

        @GetMapping("/fail/not-found")
        public void notFound() {
            throw new AccountNotFoundException(5L);
        }

        @GetMapping("/fail/insufficient-funds")
        public void insufficientFunds() {
            throw new InsufficientFundsException(5L, BigDecimal.ONE, new BigDecimal("2"));
        }

        @GetMapping("/fail/invalid")
        public void invalid() {
            throw new InvalidRequestException("Amount must be positive");
        }

        @GetMapping("/fail/concurrent-update")
        public void concurrentUpdate() {
            throw new ObjectOptimisticLockingFailureException(Account.class, 5L);
        }
    }
}
