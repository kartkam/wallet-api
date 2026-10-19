package com.example.wallet.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummary(
        LocalDate date,
        long depositCount,
        BigDecimal depositTotal,
        long transferCount,
        BigDecimal transferTotal) {
}
