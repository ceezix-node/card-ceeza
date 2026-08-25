# CardCeeza PostgreSQL Database Architecture

## 1. Overview
The CardCeeza database schema is engineered with a strict normalized design on PostgreSQL 14+, utilizing UUID v4 primary keys, cryptographic hashing, foreign key integrity, double-entry financial ledger accounting, and comprehensive audit trail logging.

---

## 2. Table Specifications

### Core Authentication & Users
| Table | Description | Primary Key | Key Relationships |
|---|---|---|---|
| `users` | User credentials, contact info, security flags | `UUID` | Linked to `roles`, `profiles`, `wallets` |
| `roles` | RBAC roles (`SUPER_ADMIN`, `ADMIN`, `VERIFIER`, `FINANCE`, `SUPPORT`, `USER`) | `UUID` | Referenced by `user_roles` |
| `user_roles` | Role assignments | `UUID` | `user_id` &rarr; `users(id)`, `role_id` &rarr; `roles(id)` |
| `sessions` | Active and revoked authentication tokens & telemetry | `UUID` | `user_id` &rarr; `users(id)` |
| `profiles` | User KYC status, encrypted identity tokens, daily trading limits | `UUID` | `user_id` &rarr; `users(id)` |

### Banking & Payout Accounts
| Table | Description | Primary Key | Key Relationships |
|---|---|---|---|
| `bank_accounts` | Verified Nigerian NIP payout accounts (masked + encrypted) | `UUID` | `user_id` &rarr; `users(id)` |

### Gift Card Catalog & Rate Engine
| Table | Description | Primary Key | Key Relationships |
|---|---|---|---|
| `gift_card_types` | Master catalog of supported brands (Apple, Steam, Razer, etc.) | `UUID` | Referenced by `gift_card_rates`, `trades` |
| `gift_card_regions` | Supported regional currencies per card type (US, UK, CA, EU) | `UUID` | `gift_card_type_id` &rarr; `gift_card_types(id)` |
| `gift_card_rates` | Live and scheduled exchange rates (Rate/Unit, Fees, Limits) | `UUID` | `gift_card_type_id` &rarr; `gift_card_types(id)` |

### Trading & Verification State Machine
| Table | Description | Primary Key | Key Relationships |
|---|---|---|---|
| `trades` | Trade submissions (`SUBMITTED` &rarr; `UNDER_REVIEW` &rarr; `VERIFIED` &rarr; `APPROVED` &rarr; `PAID` / `REJECTED`) | `UUID` | `user_id` &rarr; `users(id)`, `gift_card_type_id` &rarr; `gift_card_types(id)` |
| `trade_evidence` | Secure upload paths for card images, PIN receipts, and proofs | `UUID` | `trade_id` &rarr; `trades(id)` |
| `trade_events` | Immutable transition logs for all trade lifecycle steps | `UUID` | `trade_id` &rarr; `trades(id)`, `actor_id` &rarr; `users(id)` |
| `verification_records`| Verifier decisions, rejection codes, and inspector notes | `UUID` | `trade_id` &rarr; `trades(id)`, `verifier_id` &rarr; `users(id)` |
| `risk_assessments` | Automated fraud scoring (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) | `UUID` | `trade_id` &rarr; `trades(id)` |

### Financial Engine: Wallets, Double-Entry Ledger & Payouts
| Table | Description | Primary Key | Key Relationships |
|---|---|---|---|
| `wallets` | User available balance cache and lock state | `UUID` | `user_id` &rarr; `users(id)` |
| `ledger_accounts` | Chart of financial accounts (Liabilities, Assets, Revenue, Expenses) | `UUID` | System accounts |
| `ledger_entries` | Immutable ledger movements with idempotency keys and running balance | `UUID` | `user_id` &rarr; `users(id)`, `wallet_id` &rarr; `wallets(id)`, `trade_id` &rarr; `trades(id)` |
| `payouts` | Outbound NIP bank disbursement tracking | `UUID` | `user_id` &rarr; `users(id)`, `bank_account_id` &rarr; `bank_accounts(id)` |

### Communications & Support
| Table | Description | Primary Key | Key Relationships |
|---|---|---|---|
| `notifications` | In-app alerts for trade state changes, payouts, and security updates | `UUID` | `user_id` &rarr; `users(id)` |
| `support_tickets` | User dispute and support tickets | `UUID` | `user_id` &rarr; `users(id)` |
| `support_messages` | Threaded customer support messages | `UUID` | `ticket_id` &rarr; `support_tickets(id)` |

### Security, Auditing & Idempotency
| Table | Description | Primary Key | Key Relationships |
|---|---|---|---|
| `audit_logs` | Platform audit log tracking changes to rates, trades, users, and payouts | `UUID` | `actor_id` &rarr; `users(id)` |
| `idempotency_keys` | Prevents replay attacks and duplicate payouts on financial endpoints | `VARCHAR` | Unique idempotency key constraint |

---

## 3. Financial Integrity & Invariants
1. **Immutable Ledger Entries**: Wallet balances are strictly derived from valid ledger entries. Direct updates to wallet balances from outside transaction boundaries are forbidden.
2. **Idempotency Guarantee**: All payout creation and trade credit operations enforce unique `idempotency_key` constraints.
3. **No Plaintext Card Codes**: Sensitive e-codes and card numbers are stored encrypted with AES-256 (`e_code_encrypted`, `account_number_encrypted`).
