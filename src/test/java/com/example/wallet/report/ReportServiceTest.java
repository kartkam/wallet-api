package com.example.wallet.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.EntryType;
import com.example.wallet.service.AccountService;

@SpringBootTest
class ReportServiceTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private ReportService reportService;

    @Test
    void dailySummaryCountsDepositsAndTransfersForTheDay() {
        LocalDate today = LocalDate.now();
        DailySummary before = reportService.dailySummary(today);

        Account alice = accountService.open("alice", "USD");
        Account bob = accountService.open("bob", "USD");
        accountService.deposit(alice.getId(), new BigDecimal("100.00"));
        accountService.transfer(alice.getId(), bob.getId(), new BigDecimal("25.00"));

        DailySummary after = reportService.dailySummary(today);

        assertThat(after.depositCount() - before.depositCount()).isEqualTo(1);
        assertThat(after.depositTotal().subtract(before.depositTotal())).isEqualByComparingTo("100.00");
        assertThat(after.transferCount() - before.transferCount()).isEqualTo(1);
        assertThat(after.transferTotal().subtract(before.transferTotal())).isEqualByComparingTo("25.00");
    }

    @Test
    void dailySummaryForADayWithoutActivityIsEmpty() {
        DailySummary summary = reportService.dailySummary(LocalDate.of(2000, 1, 1));

        assertThat(summary.depositCount()).isZero();
        assertThat(summary.transferCount()).isZero();
        assertThat(summary.depositTotal()).isEqualByComparingTo("0");
    }

    @Test
    void balancesListsEveryAccountOrderedById() {
        Account first = accountService.open("carol", "EUR");
        Account second = accountService.open("dave", "EUR");

        List<BalanceRow> rows = reportService.balances();

        assertThat(rows).extracting(BalanceRow::accountId).contains(first.getId(), second.getId());
        assertThat(rows).extracting(BalanceRow::accountId).isSorted();
    }

    @Test
    void totalsByCurrencyAddsUpBalances() {
        Map<String, BigDecimal> before = reportService.totalsByCurrency();
        Account account = accountService.open("erin", "JPY");
        accountService.deposit(account.getId(), new BigDecimal("1500"));

        Map<String, BigDecimal> after = reportService.totalsByCurrency();

        BigDecimal previous = before.getOrDefault("JPY", BigDecimal.ZERO);
        assertThat(after.get("JPY").subtract(previous)).isEqualByComparingTo("1500");
    }

    @Test
    void topAccountsAreOrderedByBalanceDescending() {
        Account rich = accountService.open("frank", "CHF");
        accountService.deposit(rich.getId(), new BigDecimal("999999999.00"));

        List<BalanceRow> top = reportService.topAccounts(3);

        assertThat(top.get(0).accountId()).isEqualTo(rich.getId());
        assertThat(top).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void ledgerBetweenReturnsRowsWithOwners() {
        Account account = accountService.open("grace", "USD");
        accountService.deposit(account.getId(), new BigDecimal("42.00"));
        LocalDate today = LocalDate.now();

        List<LedgerRow> rows = reportService.ledgerBetween(today, today);

        assertThat(rows)
                .filteredOn(row -> row.accountId().equals(account.getId()))
                .singleElement()
                .satisfies(row -> {
                    assertThat(row.ownerId()).isEqualTo("grace");
                    assertThat(row.type()).isEqualTo(EntryType.DEPOSIT);
                    assertThat(row.amount()).isEqualByComparingTo("42.00");
                });
    }

    @Test
    void balancesCsvHasAHeaderAndOneLinePerAccount() {
        Account account = accountService.open("heidi", "USD");
        accountService.deposit(account.getId(), new BigDecimal("7.50"));

        String csv = reportService.balancesCsv();

        assertThat(csv).startsWith("account_id,owner_id,currency,balance\r\n");
        assertThat(csv).contains(account.getId() + ",heidi,USD,7.5");
    }

    @Test
    void ledgerCsvIncludesTheRowsForTheRequestedDays() {
        Account account = accountService.open("ivan", "USD");
        accountService.deposit(account.getId(), new BigDecimal("3.00"));
        LocalDate today = LocalDate.now();

        String csv = reportService.ledgerCsv(today, today);

        assertThat(csv).startsWith("entry_id,account_id,owner_id,type,amount,counterparty_account_id,created_at");
        assertThat(csv).contains("," + account.getId() + ",ivan,DEPOSIT,3.0000,");
    }
}
