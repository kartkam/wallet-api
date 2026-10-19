package com.example.wallet.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

@Component
public class FeeCalculator {

    private static final double FEE_RATE = 0.005;
    private static final double MINIMUM_FEE = 1.00;

    /** 0.5% of the transferred amount, rounded to cents, with a minimum of 1.00. */
    public BigDecimal feeFor(BigDecimal amount) {
        double fee = amount.doubleValue() * FEE_RATE;
        fee = Math.round(fee * 100) / 100.0;
        if (fee < MINIMUM_FEE) {
            fee = MINIMUM_FEE;
        }
        return BigDecimal.valueOf(fee);
    }
}
