package com.example.wallet.service;

import java.util.List;

public record BulkTransferResult(int succeeded, List<Failure> failures) {

    public record Failure(int index, String reason) {
    }
}
