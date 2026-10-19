package com.example.wallet.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.error.AccountNotFoundException;
import com.example.wallet.error.InvalidRequestException;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.LedgerEntryRepository;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    static final String SAME_ACCOUNT_MESSAGE = "Cannot transfer to the same account";

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;
    private final Clock clock;

    public AccountService(AccountRepository accounts, LedgerEntryRepository ledger, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.clock = clock;
    }

    @Transactional
    public Account open(String ownerId, String currency) {
        log.info("Opening {} account for owner {}", currency, ownerId);
        return accounts.save(new Account(ownerId, currency.toUpperCase(Locale.ROOT)));
    }

    @Transactional(readOnly = true)
    public Account get(Long accountId) {
        return load(accountId);
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        requirePositive(amount);
        Account account = load(accountId);
        account.credit(amount);
        ledger.save(new LedgerEntry(accountId, EntryType.DEPOSIT, amount, null, clock.instant()));
        return account;
    }

    @Transactional
    public void transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        requirePositive(amount);
        if (fromAccountId.equals(toAccountId)) {
            throw new InvalidRequestException(SAME_ACCOUNT_MESSAGE);
        }
        Account from = load(fromAccountId);
        Account to = load(toAccountId);
        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new InvalidRequestException("Currency mismatch between accounts");
        }

        from.debit(amount);
        to.credit(amount);

        Instant now = clock.instant();
        ledger.save(new LedgerEntry(fromAccountId, EntryType.TRANSFER_OUT, amount, toAccountId, now));
        ledger.save(new LedgerEntry(toAccountId, EntryType.TRANSFER_IN, amount, fromAccountId, now));
    }

    private Account load(Long accountId) {
        return accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidRequestException("Amount must be positive");
        }
    }
}
