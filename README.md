# wallet-api

A small wallet and transfers REST API built with Spring Boot 4, Spring Data JPA and an in-memory H2 database.

This repository is a sandbox for evaluating AI pull-request reviews with [Codzee](https://www.codzee.io): the `main` branch is a small, tested baseline, and each feature or fix lands through its own pull request.

## Endpoints

| Method | Path | Description |
|---|---|---|
| `POST` | `/accounts` | Open an account `{ "ownerId": "alice", "currency": "USD" }` |
| `GET` | `/accounts/{id}` | Fetch an account and its balance |
| `POST` | `/accounts/{id}/deposits` | Deposit funds `{ "amount": 25.50 }` |
| `POST` | `/transfers` | Move funds between two accounts in the same currency `{ "fromAccountId": 1, "toAccountId": 2, "amount": 10.00 }` |

Errors are returned as RFC 9457 problem details: `404` unknown account, `400` invalid request, `409` insufficient funds or a concurrent update.

### Reports

Reporting endpoints are for operations staff and require an `X-Export-Key` header.

| Method | Path | Description |
|---|---|---|
| `GET` | `/reports/daily-summary?date=2026-01-15` | Deposit and transfer counts and totals for one day |
| `GET` | `/reports/totals-by-currency` | Sum of all balances per currency |
| `GET` | `/reports/top-accounts?limit=10` | Accounts with the highest balances |
| `GET` | `/reports/balances` | CSV export of every account balance |
| `GET` | `/reports/ledger?from=2026-01-01&to=2026-01-31` | CSV export of ledger entries for a date range, both days included |

## Design notes

- Balances are `BigDecimal` with a fixed scale of 4 and are never handled as floating point.
- `Account` carries a `@Version` field, so concurrent writers to the same account get an optimistic-lock failure (`409`) instead of silently overwriting each other.
- Every deposit and transfer writes `LedgerEntry` rows in the same transaction as the balance change.
- Time comes from an injected `Clock` so it can be controlled in tests.

## Running

Requires JDK 21.

```bash
./mvnw spring-boot:run     # starts on http://localhost:8080
./mvnw verify              # builds and runs the tests
```

## Layout

```
src/main/java/com/example/wallet
  api/          controllers, request/response records, exception handler
  service/      transactional business logic
  domain/       JPA entities
  repository/   Spring Data repositories
  error/        domain exceptions
```
