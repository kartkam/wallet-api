package com.example.wallet.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.wallet.domain.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    List<LedgerEntry> findByCreatedAtBetween(Instant from, Instant to);
}
