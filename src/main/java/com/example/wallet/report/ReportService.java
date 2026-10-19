package com.example.wallet.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.LedgerEntryRepository;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private static final ZoneId REPORT_ZONE = ZoneId.systemDefault();

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;

    public ReportService(AccountRepository accounts, LedgerEntryRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @Transactional(readOnly = true)
    public DailySummary dailySummary(LocalDate date) {
        List<LedgerEntry> entries = ledger.findByCreatedAtBetween(startOf(date), startOf(date.plusDays(1)));

        long depositCount = 0;
        BigDecimal depositTotal = BigDecimal.ZERO;
        long transferCount = 0;
        BigDecimal transferTotal = BigDecimal.ZERO;

        for (LedgerEntry entry : entries) {
            switch (entry.getType()) {
                case DEPOSIT -> {
                    depositCount++;
                    depositTotal = depositTotal.add(entry.getAmount());
                }
                case TRANSFER_OUT -> {
                    transferCount++;
                    transferTotal = transferTotal.add(entry.getAmount());
                }
                case TRANSFER_IN -> {
                    // each transfer is counted once, on the sending side
                }
            }
        }
        return new DailySummary(date, depositCount, depositTotal, transferCount, transferTotal);
    }

    @Transactional(readOnly = true)
    public List<BalanceRow> balances() {
        return accounts.findAll().stream()
                .map(ReportService::toRow)
                .sorted(Comparator.comparing(BalanceRow::accountId))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> totalsByCurrency() {
        Map<String, BigDecimal> totals = new TreeMap<>();
        for (Account account : accounts.findAll()) {
            totals.merge(account.getCurrency(), account.getBalance(), BigDecimal::add);
        }
        return totals;
    }

    @Transactional(readOnly = true)
    public List<BalanceRow> topAccounts(int limit) {
        return accounts.findAll().stream()
                .map(ReportService::toRow)
                .sorted(Comparator.comparing(BalanceRow::balance).reversed())
                .limit(limit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LedgerRow> ledgerBetween(LocalDate from, LocalDate to) {
        List<LedgerEntry> entries = ledger.findByCreatedAtBetween(startOf(from), startOf(to.plusDays(1)));

        Map<Long, Account> accountsById = accounts
                .findAllById(entries.stream().map(LedgerEntry::getAccountId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        return entries.stream()
                .map(entry -> new LedgerRow(
                        entry.getId(),
                        entry.getAccountId(),
                        accountsById.get(entry.getAccountId()).getOwnerId(),
                        entry.getType(),
                        entry.getAmount(),
                        entry.getCounterpartyAccountId(),
                        entry.getCreatedAt()))
                .sorted(Comparator.comparing(LedgerRow::createdAt).thenComparing(LedgerRow::entryId))
                .toList();
    }

    public String balancesCsv() {
        List<BalanceRow> rows = balances();
        log.info("Exporting balances: {}", rows);

        return CsvWriter.write(
                List.of("account_id", "owner_id", "currency", "balance"),
                rows.stream()
                        .map(row -> List.of(
                                String.valueOf(row.accountId()),
                                row.ownerId(),
                                row.currency(),
                                row.balance().toPlainString()))
                        .toList());
    }

    public String ledgerCsv(LocalDate from, LocalDate to) {
        List<LedgerRow> rows = ledgerBetween(from, to);
        log.info("Exporting {} ledger rows from {} to {} for owners {}", rows.size(), from, to,
                rows.stream().map(LedgerRow::ownerId).distinct().toList());

        return CsvWriter.write(
                List.of("entry_id", "account_id", "owner_id", "type", "amount", "counterparty_account_id",
                        "created_at"),
                rows.stream()
                        .map(row -> List.of(
                                String.valueOf(row.entryId()),
                                String.valueOf(row.accountId()),
                                row.ownerId(),
                                row.type().name(),
                                row.amount().toPlainString(),
                                row.counterpartyAccountId() == null ? "" : row.counterpartyAccountId().toString(),
                                row.createdAt().toString()))
                        .toList());
    }

    private static Instant startOf(LocalDate date) {
        return date.atStartOfDay(REPORT_ZONE).toInstant();
    }

    private static BalanceRow toRow(Account account) {
        return new BalanceRow(account.getId(), account.getOwnerId(), account.getCurrency(), account.getBalance());
    }
}
