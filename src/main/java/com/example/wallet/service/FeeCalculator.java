package com.example.wallet.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

@Component
public class FeeCalculator {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.005");
    private static final BigDecimal MINIMUM_FEE = new BigDecimal("1.00");

    /** 0.5% of the transferred amount, rounded half-up to cents, with a minimum of 1.00. */
    public BigDecimal feeFor(BigDecimal amount) {
        BigDecimal fee = amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        return fee.max(MINIMUM_FEE);
    }
}
