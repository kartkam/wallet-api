package com.example.wallet.report;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.wallet.domain.EntryType;

public record LedgerRow(
        Long entryId,
        Long accountId,
        String ownerId,
        EntryType type,
        BigDecimal amount,
        Long counterpartyAccountId,
        Instant createdAt) {
}
