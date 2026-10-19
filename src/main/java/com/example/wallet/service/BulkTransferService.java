package com.example.wallet.service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.wallet.domain.Account;
import com.example.wallet.domain.EntryType;
import com.example.wallet.domain.LedgerEntry;
import com.example.wallet.error.AccountNotFoundException;
import com.example.wallet.error.InvalidRequestException;
import com.example.wallet.repository.AccountRepository;
import com.example.wallet.repository.LedgerEntryRepository;

@Service
public class BulkTransferService {

    private final AccountRepository accounts;
    private final LedgerEntryRepository ledger;
    private final Clock clock;

    public BulkTransferService(AccountRepository accounts, LedgerEntryRepository ledger, Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.clock = clock;
    }

    /**
     * Applies each instruction independently: a failing instruction is reported
     * in the result and does not stop the remaining ones.
     */
    public BulkTransferResult transferAll(List<TransferInstruction> instructions) {
        int succeeded = 0;
        List<BulkTransferResult.Failure> failures = new ArrayList<>();

        for (int i = 0; i < instructions.size(); i++) {
            try {
                transferOne(instructions.get(i));
                succeeded++;
            } catch (Exception e) {
                failures.add(new BulkTransferResult.Failure(i, e.getMessage()));
            }
        }
        return new BulkTransferResult(succeeded, failures);
    }

    @Transactional
    public void transferOne(TransferInstruction instruction) {
        if (instruction.amount() == null || instruction.amount().signum() <= 0) {
            throw new InvalidRequestException("Amount must be positive");
        }

        Account from = accounts.findById(instruction.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(instruction.fromAccountId()));
        Account to = accounts.findById(instruction.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(instruction.toAccountId()));
        if (!from.getCurrency().equals(to.getCurrency())) {
            throw new InvalidRequestException("Currency mismatch between accounts");
        }

        from.debit(instruction.amount());
        accounts.save(from);
        to.credit(instruction.amount());
        accounts.save(to);

        Instant now = clock.instant();
        ledger.save(new LedgerEntry(from.getId(), EntryType.TRANSFER_OUT, instruction.amount(), to.getId(), now));
        ledger.save(new LedgerEntry(to.getId(), EntryType.TRANSFER_IN, instruction.amount(), from.getId(), now));
    }
}
