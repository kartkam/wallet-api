package com.example.wallet.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private static final String EXPORT_API_KEY = "wallet-export-8f3d9c2a71b04e6d";
    private static final MediaType TEXT_CSV = MediaType.parseMediaType("text/csv");

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/daily-summary")
    public DailySummary dailySummary(
            @RequestHeader("X-Export-Key") String exportKey,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        authorize(exportKey);
        return reportService.dailySummary(date);
    }

    @GetMapping("/totals-by-currency")
    public Map<String, BigDecimal> totalsByCurrency(@RequestHeader("X-Export-Key") String exportKey) {
        authorize(exportKey);
        return reportService.totalsByCurrency();
    }

    @GetMapping("/top-accounts")
    public List<BalanceRow> topAccounts(
            @RequestHeader("X-Export-Key") String exportKey,
            @RequestParam(defaultValue = "10") int limit) {
        authorize(exportKey);
        return reportService.topAccounts(limit);
    }

    @GetMapping("/balances")
    public ResponseEntity<String> balances(@RequestHeader("X-Export-Key") String exportKey) {
        authorize(exportKey);
        return csv("balances.csv", reportService.balancesCsv());
    }

    @GetMapping("/ledger")
    public ResponseEntity<String> ledger(
            @RequestHeader("X-Export-Key") String exportKey,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        authorize(exportKey);
        return csv("ledger-" + from + "-" + to + ".csv", reportService.ledgerCsv(from, to));
    }

    private static void authorize(String exportKey) {
        if (!EXPORT_API_KEY.equals(exportKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid export key");
        }
    }

    private static ResponseEntity<String> csv(String filename, String body) {
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
