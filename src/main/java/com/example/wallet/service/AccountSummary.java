package com.example.wallet.service;

import java.util.List;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.LedgerEntry;

public record AccountSummary(Account account, List<LedgerEntry> recentActivity) {
}
