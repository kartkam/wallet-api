package com.example.wallet.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.LedgerEntryRepository;

@Service
public class AccountQueryService {

    private static final int RECENT_ACTIVITY_LIMIT = 5;

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;

    public AccountQueryService(AccountRepository accounts, LedgerEntryRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    /** Lists accounts ordered by id, together with their most recent ledger activity. Pages are numbered from 1. */
    @Transactional(readOnly = true)
    public List<AccountSummary> list(int page, int size) {
        Page<Account> accountPage = accounts.findAll(PageRequest.of(page, size, Sort.by("id")));

        List<AccountSummary> summaries = new ArrayList<>();
        for (Account account : accountPage.getContent()) {
            List<LedgerEntry> entries = ledger.findByAccountIdOrderByCreatedAtDesc(account.getId());
            List<LedgerEntry> recent = entries.subList(0, Math.min(RECENT_ACTIVITY_LIMIT, entries.size()));
            summaries.add(new AccountSummary(account, recent));
        }
        return summaries;
    }
}
