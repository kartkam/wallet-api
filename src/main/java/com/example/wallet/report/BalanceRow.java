package com.example.wallet.report;

import java.math.BigDecimal;

public record BalanceRow(Long accountId, String ownerId, String currency, BigDecimal balance) {
}
