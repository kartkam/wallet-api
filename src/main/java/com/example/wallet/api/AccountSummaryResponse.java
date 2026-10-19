package com.example.wallet.api;

import java.math.BigDecimal;
import java.util.List;

import com.example.wallet.service.AccountSummary;

public record AccountSummaryResponse(
        Long id,
        String ownerId,
        String currency,
        BigDecimal balance,
        List<AccountActivityResponse> recentActivity) {

    public static AccountSummaryResponse from(AccountSummary summary) {
        return new AccountSummaryResponse(
                summary.account().getId(),
                summary.account().getOwnerId(),
                summary.account().getCurrency(),
                summary.account().getBalance(),
                summary.recentActivity().stream().map(AccountActivityResponse::from).toList());
    }
}
