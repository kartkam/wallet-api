package com.example.wallet.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.service.StatementService;

@RestController
public class StatementController {

    private static final Logger log = LoggerFactory.getLogger(StatementController.class);

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping("/accounts/{id}/statement")
    public List<StatementEntryResponse> statement(
            @PathVariable Long id,
            @RequestHeader("X-Owner-Id") String ownerId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer limit) {
        log.debug("Statement requested for account {} by owner {}", id, ownerId);
        return statementService.statement(id, type, limit).stream()
                .map(StatementEntryResponse::from)
                .toList();
    }
}
