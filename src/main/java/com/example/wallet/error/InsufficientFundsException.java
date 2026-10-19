package com.example.wallet.error;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long accountId, BigDecimal balance, BigDecimal requested) {
        super("Insufficient funds in account " + accountId + ": balance " + balance.toPlainString()
                + ", requested " + requested.toPlainString());
    }
}
