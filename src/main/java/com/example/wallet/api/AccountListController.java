package com.example.wallet.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.wallet.service.AccountQueryService;

@RestController
public class AccountListController {

    private final AccountQueryService queryService;

    public AccountListController(AccountQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/accounts")
    public List<AccountSummaryResponse> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return queryService.list(page, size).stream()
                .map(AccountSummaryResponse::from)
                .toList();
    }
}
