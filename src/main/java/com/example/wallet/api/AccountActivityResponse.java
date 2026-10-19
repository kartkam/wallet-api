package com.example.wallet.api;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;

public record AccountActivityResponse(EntryType type, BigDecimal amount, Instant createdAt) {

    public static AccountActivityResponse from(LedgerEntry entry) {
        return new AccountActivityResponse(entry.getType(), entry.getAmount(), entry.getCreatedAt());
    }
}
