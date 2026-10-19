package com.example.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.error.AccountNotFoundException;

@SpringBootTest
class StatementServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private StatementService statementService;

    @Test
    void statementReturnsAllEntriesForTheAccount() {
        Account alice = accountService.open("alice", "USD");
        Account bob = accountService.open("bob", "USD");
        accountService.deposit(alice.getId(), new BigDecimal("100.00"));
        accountService.deposit(alice.getId(), new BigDecimal("50.00"));
        accountService.transfer(alice.getId(), bob.getId(), new BigDecimal("20.00"));

        assertThat(statementService.statement(alice.getId(), null, null)).hasSize(3);
        assertThat(statementService.statement(bob.getId(), null, null)).hasSize(1);
    }

    @Test
    void statementCanBeFilteredByType() {
        Account alice = accountService.open("alice", "USD");
        Account bob = accountService.open("bob", "USD");
        accountService.deposit(alice.getId(), new BigDecimal("100.00"));
        accountService.deposit(alice.getId(), new BigDecimal("50.00"));
        accountService.transfer(alice.getId(), bob.getId(), new BigDecimal("20.00"));

        assertThat(statementService.statement(alice.getId(), "DEPOSIT", null))
                .hasSize(2)
                .extracting(LedgerEntry::getType)
                .containsOnly(EntryType.DEPOSIT);
    }

    @Test
    void statementHonoursTheLimit() {
        Account alice = accountService.open("alice", "USD");
        accountService.deposit(alice.getId(), new BigDecimal("1.00"));
        accountService.deposit(alice.getId(), new BigDecimal("2.00"));
        accountService.deposit(alice.getId(), new BigDecimal("3.00"));

        assertThat(statementService.statement(alice.getId(), null, 2)).hasSize(2);
    }

    @Test
    void statementFailsForUnknownAccount() {
        assertThatThrownBy(() -> statementService.statement(-1L, null, null))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
