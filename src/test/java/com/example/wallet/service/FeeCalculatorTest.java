package com.example.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class FeeCalculatorTest {

    private final FeeCalculator calculator = new FeeCalculator();

    @Test
    void chargesHalfAPercent() {
        assertThat(calculator.feeFor(new BigDecimal("1000.00"))).isEqualByComparingTo("5.00");
        assertThat(calculator.feeFor(new BigDecimal("400.00"))).isEqualByComparingTo("2.00");
    }

    @Test
    void appliesTheMinimumFee() {
        assertThat(calculator.feeFor(new BigDecimal("10.00"))).isEqualByComparingTo("1.00");
        assertThat(calculator.feeFor(new BigDecimal("0.01"))).isEqualByComparingTo("1.00");
    }

    @Test
    void roundsToCents() {
        assertThat(calculator.feeFor(new BigDecimal("2500.50"))).isEqualByComparingTo("12.50");
    }
}
