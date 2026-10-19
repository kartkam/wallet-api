package com.example.wallet.api;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BulkTransferRequest(@NotEmpty @Size(max = 100) List<@Valid TransferRequest> transfers) {
}
