package com.example.wallet.api;

import java.math.BigDecimal;

import com.example.wallet.domain.Account;

public record AccountResponse(Long id, String ownerId, String currency, BigDecimal balance) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getOwnerId(), account.getCurrency(),
                account.getBalance());
    }
}
