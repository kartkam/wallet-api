package com.example.wallet.service;

import java.math.BigDecimal;

public record TransferInstruction(Long fromAccountId, Long toAccountId, BigDecimal amount) {
}
