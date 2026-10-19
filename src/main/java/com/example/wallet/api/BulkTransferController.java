package com.example.wallet.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.service.BulkTransferResult;
import com.example.wallet.service.BulkTransferService;
import com.example.wallet.service.TransferInstruction;

import jakarta.validation.Valid;

@RestController
public class BulkTransferController {

    private final BulkTransferService bulkTransferService;

    public BulkTransferController(BulkTransferService bulkTransferService) {
        this.bulkTransferService = bulkTransferService;
    }

    @PostMapping("/transfers/bulk")
    public BulkTransferResult transferAll(@Valid @RequestBody BulkTransferRequest request) {
        return bulkTransferService.transferAll(request.transfers().stream()
                .map(t -> new TransferInstruction(t.fromAccountId(), t.toAccountId(), t.amount()))
                .toList());
    }
}
