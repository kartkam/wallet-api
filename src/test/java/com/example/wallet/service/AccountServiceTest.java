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
import com.example.wallet.error.InsufficientFundsException;
import com.example.wallet.error.InvalidRequestException;
import com.example.wallet.repository.LedgerEntryRepository;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService service;

    @Autowired
    private LedgerEntryRepository ledger;

    @Test
    void openNormalisesCurrencyToUpperCase() {
        Account account = service.open("alice", "usd");

        assertThat(account.getId()).isNotNull();
        assertThat(account.getCurrency()).isEqualTo("USD");
        assertThat(account.getBalance()).isEqualByComparingTo("0");
    }

    @Test
    void depositIncreasesBalanceAndWritesLedgerEntry() {
        Account account = service.open("alice", "USD");

        Account updated = service.deposit(account.getId(), new BigDecimal("25.50"));

        assertThat(updated.getBalance()).isEqualByComparingTo("25.50");
        assertThat(ledger.findByAccountIdOrderByCreatedAtDesc(account.getId()))
                .singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getType()).isEqualTo(EntryType.DEPOSIT);
                    assertThat(entry.getAmount()).isEqualByComparingTo("25.50");
                });
    }

    @Test
    void depositRejectsNonPositiveAmount() {
        Account account = service.open("alice", "USD");

        assertThatThrownBy(() -> service.deposit(account.getId(), BigDecimal.ZERO))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.deposit(account.getId(), new BigDecimal("-1")))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void getFailsForUnknownAccount() {
        assertThatThrownBy(() -> service.get(-1L)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void transferMovesFundsAndWritesBothLedgerEntries() {
        Account from = service.open("alice", "USD");
        Account to = service.open("bob", "USD");
        service.deposit(from.getId(), new BigDecimal("100.00"));

        service.transfer(from.getId(), to.getId(), new BigDecimal("40.00"));

        // 40.00 transferred plus the 1.00 minimum fee
        assertThat(service.get(from.getId()).getBalance()).isEqualByComparingTo("59.00");
        assertThat(service.get(to.getId()).getBalance()).isEqualByComparingTo("40.00");
        assertThat(ledger.findByAccountIdOrderByCreatedAtDesc(from.getId()))
                .extracting(LedgerEntry::getType)
                .contains(EntryType.TRANSFER_OUT);
        assertThat(ledger.findByAccountIdOrderByCreatedAtDesc(to.getId()))
                .extracting(LedgerEntry::getType)
                .containsExactly(EntryType.TRANSFER_IN);
    }

    @Test
    void transferWithInsufficientFundsChangesNothing() {
        Account from = service.open("alice", "USD");
        Account to = service.open("bob", "USD");
        service.deposit(from.getId(), new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.transfer(from.getId(), to.getId(), new BigDecimal("10.01")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(service.get(from.getId()).getBalance()).isEqualByComparingTo("10.00");
        assertThat(service.get(to.getId()).getBalance()).isEqualByComparingTo("0");
        assertThat(ledger.findByAccountIdOrderByCreatedAtDesc(to.getId())).isEmpty();
    }

    @Test
    void transferRejectsSameAccountAndCurrencyMismatch() {
        Account usd = service.open("alice", "USD");
        Account eur = service.open("bob", "EUR");
        service.deposit(usd.getId(), new BigDecimal("50.00"));

        assertThatThrownBy(() -> service.transfer(usd.getId(), usd.getId(), BigDecimal.ONE))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.transfer(usd.getId(), eur.getId(), BigDecimal.ONE))
                .isInstanceOf(InvalidRequestException.class);
    }
}
