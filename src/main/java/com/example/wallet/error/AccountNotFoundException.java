package com.example.wallet.error;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId) {
        super("Account " + accountId + " not found");
    }
}
