package com.example.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.wallet.domain.Account;

@SpringBootTest
class BulkTransferServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private BulkTransferService bulkTransferService;

    @Test
    void appliesEveryInstruction() {
        Account alice = accountService.open("alice", "USD");
        Account bob = accountService.open("bob", "USD");
        Account carol = accountService.open("carol", "USD");
        accountService.deposit(alice.getId(), new BigDecimal("100.00"));

        BulkTransferResult result = bulkTransferService.transferAll(List.of(
                new TransferInstruction(alice.getId(), bob.getId(), new BigDecimal("30.00")),
                new TransferInstruction(alice.getId(), carol.getId(), new BigDecimal("20.00"))));

        assertThat(result.succeeded()).isEqualTo(2);
        assertThat(result.failures()).isEmpty();
        assertThat(accountService.get(alice.getId()).getBalance()).isEqualByComparingTo("50.00");
        assertThat(accountService.get(bob.getId()).getBalance()).isEqualByComparingTo("30.00");
        assertThat(accountService.get(carol.getId()).getBalance()).isEqualByComparingTo("20.00");
    }

    @Test
    void reportsFailuresWithoutStoppingTheBatch() {
        Account alice = accountService.open("alice", "USD");
        Account bob = accountService.open("bob", "USD");
        accountService.deposit(alice.getId(), new BigDecimal("10.00"));

        BulkTransferResult result = bulkTransferService.transferAll(List.of(
                new TransferInstruction(alice.getId(), bob.getId(), new BigDecimal("500.00")),
                new TransferInstruction(alice.getId(), bob.getId(), new BigDecimal("4.00"))));

        assertThat(result.succeeded()).isEqualTo(1);
        assertThat(result.failures()).hasSize(1);
        assertThat(result.failures().get(0).index()).isZero();
        assertThat(accountService.get(bob.getId()).getBalance()).isEqualByComparingTo("4.00");
    }

    @Test
    void reportsUnknownAccounts() {
        Account alice = accountService.open("alice", "USD");

        BulkTransferResult result = bulkTransferService.transferAll(List.of(
                new TransferInstruction(alice.getId(), -1L, BigDecimal.ONE)));

        assertThat(result.succeeded()).isZero();
        assertThat(result.failures()).singleElement()
                .satisfies(failure -> assertThat(failure.reason()).contains("not found"));
    }
}
