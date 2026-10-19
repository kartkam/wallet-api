package com.example.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.wallet.domain.Account;

@SpringBootTest
class AccountQueryServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountQueryService queryService;

    @Test
    void listIncludesBalanceAndRecentActivity() {
        Account account = accountService.open("alice", "USD");
        accountService.deposit(account.getId(), new BigDecimal("10.00"));
        accountService.deposit(account.getId(), new BigDecimal("5.00"));

        AccountSummary summary = find(queryService.list(0, 1000), account.getId());

        assertThat(summary.account().getBalance()).isEqualByComparingTo("15.00");
        assertThat(summary.recentActivity()).hasSize(2);
    }

    @Test
    void recentActivityIsLimitedToTheFiveNewestEntries() {
        Account account = accountService.open("bob", "USD");
        for (int i = 0; i < 8; i++) {
            accountService.deposit(account.getId(), BigDecimal.ONE);
        }

        AccountSummary summary = find(queryService.list(0, 1000), account.getId());

        assertThat(summary.recentActivity()).hasSize(5);
    }

    @Test
    void accountsWithoutActivityHaveAnEmptyActivityList() {
        Account account = accountService.open("carol", "EUR");

        AccountSummary summary = find(queryService.list(0, 1000), account.getId());

        assertThat(summary.recentActivity()).isEmpty();
    }

    private static AccountSummary find(List<AccountSummary> summaries, Long accountId) {
        return summaries.stream()
                .filter(s -> s.account().getId().equals(accountId))
                .findFirst()
                .orElseThrow();
    }
}
