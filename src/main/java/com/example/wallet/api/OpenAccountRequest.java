package com.example.wallet.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OpenAccountRequest(
        @NotBlank String ownerId,
        @NotBlank @Size(min = 3, max = 3) String currency) {
}
