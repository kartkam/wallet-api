package com.example.wallet.api;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;

public record StatementEntryResponse(
        Long id,
        EntryType type,
        BigDecimal amount,
        Long counterpartyAccountId,
        Instant createdAt) {

    public static StatementEntryResponse from(LedgerEntry entry) {
        return new StatementEntryResponse(entry.getId(), entry.getType(), entry.getAmount(),
                entry.getCounterpartyAccountId(), entry.getCreatedAt());
    }
}
