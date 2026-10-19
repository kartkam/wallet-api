package com.example.wallet.service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.error.AccountNotFoundException;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.LedgerEntryRepository;

@Service
public class StatementService {

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;

    public StatementService(AccountRepository accounts, LedgerEntryRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> statement(Long accountId, String type, Integer limit) {
        accounts.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId));

        Stream<LedgerEntry> entries = ledger.findByAccountIdOrderByCreatedAtDesc(accountId).stream();
        if (type != null && !type.isBlank()) {
            EntryType wanted = EntryType.valueOf(type.toUpperCase(Locale.ROOT));
            entries = entries.filter(entry -> entry.getType() == wanted);
        }
        if (limit != null) {
            entries = entries.limit(limit);
        }
        return entries.toList();
    }
}
